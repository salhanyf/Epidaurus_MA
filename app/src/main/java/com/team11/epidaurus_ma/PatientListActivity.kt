package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

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

    private suspend fun getRows(supabase: SupabaseClient, metadata:JSONObject?): List<PatientDBResponse>? {
        val response = supabase.from("Patients").select(){
            filter {
                eq("department", metadata?.get("Department ID").toString())
                eq("floor", metadata?.get("Floor ID").toString())
            }
        }.decodeList<PatientDBResponse>()
        return response
    }
}