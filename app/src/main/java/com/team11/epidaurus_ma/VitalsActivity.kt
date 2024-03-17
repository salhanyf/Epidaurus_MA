package com.team11.epidaurus_ma

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        //status = false
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
        setContentView(R.layout.activity_vitals)

        val nameTextView = findViewById<TextView>(R.id.NameTextView)
        val deviceTextView = findViewById<TextView>(R.id.deviceID)
        val time_date = findViewById<TextView>(R.id.time_date)
        time_date.text = getCurrentDateTimeAsString()
        val fallValueTextView = findViewById<TextView>(R.id.FallValue)
        val heartateTV = findViewById<TextView>(R.id.heart_rateValue)
        val spo2TV = findViewById<TextView>(R.id.blood_oxygenValue)
        val bodyTempCTV = findViewById<TextView>(R.id.temperature_in_C_Value)
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
               val value = supabaseFetch()
                val fahrenheit = celisusToF(value.bodyTemp)
                deviceTextView.text = value.id.toString()
                heartateTV.text = value.heartRate.toString()
                spo2TV.text = value.spO2.toString()
                bodyTempCTV.text = value.bodyTemp.toString()
                //bodyTempFTV.text = fahrenheit.toString()
                if (value.fallDetected != 0){
                    fallValueTextView.text = "Detected"
                    fallValueTextView.setTextColor(ContextCompat.getColor(this@VitalsActivity, R.color.error_red))
                }
            }
        }
    }

    private fun getCurrentDateTimeAsString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    private suspend fun supabaseFetch():VitalsResponse{
        val value = supabase.from("ESP32FallDetection").select(){
        }.decodeSingle<VitalsResponse>()
        return value
    }

    private fun celisusToF(celsius:Float):Float{
        return 1.8f*celsius+32f
    }
}