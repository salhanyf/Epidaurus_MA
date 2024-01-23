package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
        //var data: List<PatientDBResponse>
        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
        ) {
            install(Postgrest)
        }

        /*if (intent.hasExtra("nurseMetadata")) {
            val nurseMetadataString = intent.getStringExtra("nurseMetadata")
            val metadataJson = JSONObject(nurseMetadataString.toString())
        }*/
        launch{
            getRows(supabase)
        }


    }

    private suspend fun getRows(supabase: SupabaseClient){
        val response = supabase.from("Patients").select().decodeList<PatientDBResponse>()
        Log.e("Supabase", response.toString())
    }
}