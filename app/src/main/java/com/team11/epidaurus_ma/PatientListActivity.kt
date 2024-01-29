package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class PatientListActivity : AppCompatActivity(), CoroutineScope{
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        var data: List<PatientDBResponse>? = null
        var metadataJSON: JSONObject? = null
        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
        ) {
            install(Postgrest)
        }

        if (intent.hasExtra("nurseMetadata")) {
            val nurseMetadataString = intent.getStringExtra("nurseMetadata")
            metadataJSON = JSONObject(nurseMetadataString.toString())
        }
        launch{
            data = getRows(supabase, metadataJSON)
            val patientList = mutableListOf<PatientDBResponse>()
            data?.forEach { patientList.add(it) }
            val recyclerView = findViewById<RecyclerView>(R.id.patientListRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@PatientListActivity)
            recyclerView.adapter = PatientAdapter(patientList)
        }
    }

    private suspend fun getRows(supabase: SupabaseClient, metadata:JSONObject?): List<PatientDBResponse>? {
        val response = supabase.from("Patients").select(){
            filter {
                eq("department", metadata?.get("Department ID").toString())
                eq("floor", metadata?.get("Floor ID").toString())
            }
        }.decodeList<PatientDBResponse>()
        supabase.close()
        return response
    }
}