package com.team11.epidaurus_ma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

class PatientDetailsActivity : AppCompatActivity(), CoroutineScope {

    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ){
        install(Auth)
        install(Postgrest)
    }

    private var job: Job = Job()
    private var issueId: String = "0"
    private var selectedIssue: String = "Other"
    private var selectedSeverity: String = "Low"
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
        setContentView(R.layout.activity_patient_details)

        val patientId = intent.getIntExtra("id",0)
        val name = intent.getStringExtra("name").toString()
        val dob = intent.getStringExtra("dob").toString()
        val department = intent.getStringExtra("department").toString()
        val roomNumber = intent.getStringExtra("roomNumber").toString()
        val symptoms = intent.getStringExtra("symptoms").toString()
        val medicalHistory = intent.getStringExtra("medicalHistory").toString()
        val medicalDiagnosis = intent.getStringExtra("medicalDiagnosis").toString()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val reportIssueBtn = findViewById<ImageView>(R.id.reportIssue)
        reportIssueBtn.setOnClickListener{
            showReportingIssuesDialog(supabase,name)
        }

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
            vitalsIntent.putExtra("name", name)
            startActivity(vitalsIntent)
        }

        notesBtn.setOnClickListener {
            val notesIntent = Intent(this, NotesActivity::class.java)
            notesIntent.putExtra("patientId", patientId)
            notesIntent.putExtra("name", name)
            startActivity(notesIntent)
        }
    }

    private fun showReportingIssuesDialog(supabase:SupabaseClient, name:String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_issue, null)
        val IssueSpinner: Spinner = dialogView.findViewById(R.id.issueEditText)
        val SeveritySpinner: Spinner = dialogView.findViewById(R.id.severityEditText)
        val input = dialogView.findViewById<EditText>(R.id.issueContentEditText)

        val issues = resources.getStringArray(R.array.issues_array).toList()
        setupSpinner(dialogView, IssueSpinner, issues,"Select an issue to report") { selected, _ ->
            selectedIssue = selected
        }

        val severityLevels = resources.getStringArray(R.array.severity_levels_array).toList()
        setupSpinner(dialogView, SeveritySpinner,severityLevels,"Select severity") { selected, _ ->
            selectedSeverity = selected
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Report", null)
            .setCancelable(true)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val observationText = input.text.toString()
            var isValid = true

            if (selectedIssue == "Select an issue to report") {
                Toast.makeText(this, "Please select an issue to report.", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (selectedSeverity == "Select severity") {
                Toast.makeText(this, "Please select issue severity.", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (selectedIssue == "Other" && observationText.isBlank()) {
                Toast.makeText(this, "Please describe the issue.", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (isValid) {
                val issueText = if (selectedIssue == "Other") observationText else selectedIssue
                Toast.makeText(this, "Issue reported: $selectedIssue with severity $selectedSeverity", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                launch {
                    val nurseEmail = supabase.auth.retrieveUserForCurrentSession().email
                    val date = getCurrentDateTimeAsString()
                    val issueEntry = IssueEntry(nurseEmail, date, issueText, name, selectedSeverity)
                    supabase.from("Reporting").insert(issueEntry)
                }
            }
        }
    }

    private fun setupSpinner(dialogView: View, spinner: Spinner, options: List<String>, hint: String, onSelect: (String, Int) -> Unit) {
        val adapter = AdapterIssues(this, R.layout.dropdown_item, options, hint)
        spinner.adapter = adapter
        spinner.dropDownVerticalOffset = 150
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                val input = dialogView.findViewById<EditText>(R.id.issueContentEditText)

                if (selected == "Other") {
                    input.visibility = View.VISIBLE
                } else {
                    input.visibility = View.GONE
                }
                if (position > 0) {
                    adapter.setCurrentSelection(selected)
                }
                onSelect(selected, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                adapter.setCurrentSelection(hint)
            }
        }
    }

    private fun getCurrentDateTimeAsString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}