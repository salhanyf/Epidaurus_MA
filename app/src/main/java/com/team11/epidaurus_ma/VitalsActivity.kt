package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

class VitalsActivity : AppCompatActivity(), CoroutineScope {
    private var status: Boolean = true
    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ){
        install(Auth)
        install(Postgrest)
    }
    private var patientIDText: String? = null
    private var patientNameText: String? = null

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        status = false
        launch{
            supabase.close()
        }
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vitals)

        val nameTextView = findViewById<TextView>(R.id.NameTextView)
        val fallValueTextView = findViewById<TextView>(R.id.FallValue)
        var patientId:Int = 0

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        if (intent.hasExtra("patientId")){
            patientId = intent.getIntExtra("patientId",0)
            patientIDText = patientId.toString()
            Log.d("VitalsActivity", "Patient ID: $patientIDText")
        }
        if (intent.hasExtra("name")){
            patientNameText = intent.getStringExtra("name")
            nameTextView.text = patientNameText
            Log.d("VitalsActivity", "Patient Name: $patientNameText")
        }
        launch {
            while (status) {
                fallValueTextView.text = supabaseFetch().touched.toString()
                Log.e("This ain't stoppin","yeah")
                sleep(2500)
            }
        }
    }

    private fun getCurrentDateTimeAsString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    private suspend fun supabaseFetch():CapacitiveTouchResponse{
        val value = supabase.from("ESP32CapacitiveTouch").select(){
            filter {
                eq("id", 1)
            }
        }.decodeSingle<CapacitiveTouchResponse>()

        return value
    }
}