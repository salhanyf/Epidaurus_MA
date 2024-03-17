package com.team11.epidaurus_ma

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.team11.epidaurus_ma.databinding.ActivityAlertBinding
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AlertActivity : AppCompatActivity(), CoroutineScope {
    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ){
        install(Auth)
        install(Postgrest)
    }

    private lateinit var binding: ActivityAlertBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val deviceId = intent.getIntExtra("deviceId", 0)
        val patientId = intent.getIntExtra("patientId", 0)

        // Ensure the activity is displayed even when the screen is locked.
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        if (patientId != 0) {
            fetchPatientData(patientId)
        }

        binding.btnDismiss.setOnClickListener {
            finish() // Close the activity
        }
    }


    private fun fetchPatientData(patientId: Int) {
        // Asynchronously fetch patient data
        launch {
            val patientData = withContext(Dispatchers.IO) {
                supabaseFetchPatientData(patientId)
            }
            // Update the UI with the fetched data
            patientData?.let { patient ->
                with(binding) {
                    patientDetailsNameTV.text = patient.name
                    patientDetailsDobTV.text = patient.date_of_birth
                    patientDetailsDepartmentTV.text = patient.department
                    patientDetailsRoomNumTV.text = patient.room_number

                    btnProfile.setOnClickListener {
                        val profileIntent = Intent(this@AlertActivity, PatientDetailsActivity::class.java).apply {
                            putExtra("id", patient.id)
                            putExtra("name", patient.name)
                            putExtra("dob", patient.date_of_birth)
                            putExtra("department", patient.department)
                            putExtra("roomNumber", patient.room_number)
                            putExtra("symptoms", patient.symptoms)
                            putExtra("medicalHistory", patient.medical_history)
                            putExtra("medicalDiagnosis", patient.medical_diagnosis)
                        }
                        startActivity(profileIntent)
                    }
                }
            }
        }
    }

    private suspend fun supabaseFetchPatientData(patientId: Int): PatientDBResponse? {
        val response = supabase.from("Patients").select(){
            filter { eq("id", patientId) }
        }.decodeSingle<PatientDBResponse>()
        return response
    }
}
