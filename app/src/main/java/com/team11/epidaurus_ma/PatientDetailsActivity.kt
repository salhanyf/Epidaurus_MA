package com.team11.epidaurus_ma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class PatientDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val patientId = intent.getIntExtra("id",0)
        val name = intent.getStringExtra("name").toString()
        val dob = intent.getStringExtra("dob").toString()
        val department = intent.getStringExtra("department").toString()
        val roomNumber = intent.getStringExtra("roomNumber").toString()
        val symptoms = intent.getStringExtra("symptoms").toString()
        val medicalHistory = intent.getStringExtra("medicalHistory").toString()
        val medicalDiagnosis = intent.getStringExtra("medicalDiagnosis").toString()

        val nameTV = findViewById<TextView>(R.id.patientDetails_nameTV)
        val dobTV = findViewById<TextView>(R.id.patientDetails_dobTV)
        val departmentTV = findViewById<TextView>(R.id.patientDetails_departmentTV)
        val roomNumTV = findViewById<TextView>(R.id.patientDetails_roomNumTV)
        val symptomsTV = findViewById<TextView>(R.id.patientDetails_symptomsTV)
        val medicHistTV = findViewById<TextView>(R.id.patientDetails_medicHistTv)
        val medicDiagTV = findViewById<TextView>(R.id.patientDetails_medicDiagTV)
        val vitalsBtn = findViewById<Button>(R.id.vitalsButton)
        val notesBtn = findViewById<Button>(R.id.notesButton)

        nameTV.text = name
        dobTV.text = dob
        departmentTV.text = department
        roomNumTV.text = roomNumber
        symptomsTV.text = symptoms
        medicHistTV.text = medicalHistory
        medicDiagTV.text = medicalDiagnosis

        vitalsBtn.setOnClickListener{
            val vitalsIntent = Intent(this, VitalsActivity::class.java)
            vitalsIntent.putExtra("patientId", patientId)
            startActivity(vitalsIntent)
        }

        notesBtn.setOnClickListener {
            val notesIntent = Intent(this, NotesActivity::class.java)
            notesIntent.putExtra("patientId", patientId)
            startActivity(notesIntent)
        }
    }
}