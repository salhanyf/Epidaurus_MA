package com.team11.epidaurus_ma

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class PatientListActivity : AppCompatActivity(), CoroutineScope{

    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ){
        install(Auth)
        install(Postgrest)
    }

    private var nurseName: String? = null
    private var nurseDep: String? = null
    private var nurseFloor: String? = null

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        launch{
            supabase.close()
        }
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        val nameTextView = findViewById<TextView>(R.id.NameTextView)
        val departmentTextView = findViewById<TextView>(R.id.DepTextView)
        val floorTextView = findViewById<TextView>(R.id.FloorTextView)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val searchButton = findViewById<ImageView>(R.id.searchButton)
        searchButton.setOnClickListener{
            if (searchEditText.visibility == View.GONE) {
                searchEditText.visibility = View.VISIBLE
                searchEditText.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            } else {
                searchEditText.visibility = View.GONE
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            }
        }

        searchEditText.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Fetch and filter patient list based on search query
                launch {
                    val searchQuery = s.toString()
                    val nurseMetadata = fetchUserData(supabase)
                    val data = getRows(supabase, nurseMetadata, searchQuery)
                    val patientList = data.orEmpty()
                    val recyclerView = findViewById<RecyclerView>(R.id.patientListRecyclerView)
                    recyclerView.adapter = PatientAdapter(patientList)
                }
            }
        })

        launch{
            val nurseMetadata = fetchUserData(supabase)
            nurseName = nurseMetadata?.get("name").toString()
            nurseDep = nurseMetadata?.getString("Department ID") ?: ""
            nurseFloor = nurseMetadata?.getString("Floor ID") ?: ""

            nameTextView.text = nurseName
            departmentTextView.text = nurseDep
            floorTextView.text = nurseFloor

            var data: List<PatientDBResponse>? = null
            data = getRows(supabase, nurseMetadata)
            val patientList = mutableListOf<PatientDBResponse>()
            data?.forEach { patientList.add(it) }
            val recyclerView = findViewById<RecyclerView>(R.id.patientListRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@PatientListActivity)
            recyclerView.adapter = PatientAdapter(patientList)
        }
    }

    private suspend fun fetchUserData(supabase: SupabaseClient): JSONObject? {
        val metadata = supabase.auth.retrieveUserForCurrentSession()?.userMetadata.toString()
        var metadataJSON: JSONObject? = null
        metadataJSON = JSONObject(metadata)
        return metadataJSON
    }

//    private suspend fun getRows(supabase: SupabaseClient, metadata:JSONObject?): List<PatientDBResponse>? {
//        val response = supabase.from("Patients").select(){
//            filter {
//                eq("department", metadata?.get("Department ID").toString())
//                eq("floor", metadata?.get("Floor ID").toString())
//            }
//        }.decodeList<PatientDBResponse>()
//        return response
//    }

    private suspend fun getRows(supabase: SupabaseClient, metadata:JSONObject?, searchQuery: String? = null): List<PatientDBResponse>? {
        var response = supabase.from("Patients").select(){
            filter {
                eq("department", metadata?.get("Department ID").toString())
                eq("floor", metadata?.get("Floor ID").toString())
            }
        }
        if (!searchQuery.isNullOrBlank()) {
            response = supabase.from("Patients").select() {
                filter {
                    eq("department", metadata?.get("Department ID").toString())
                    eq("floor", metadata?.get("Floor ID").toString())
                    textSearch("name", "'$searchQuery$'", TextSearchType.WEBSEARCH)
                }
            }
        }
        return response.decodeList<PatientDBResponse>()
    }
}