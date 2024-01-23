package com.team11.epidaurus_ma


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import androidx.appcompat.widget.Toolbar

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val toolbar: Toolbar = findViewById(R.id.app_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        //Logic for accessing the patient list activity
        val patientsBtn = findViewById<Button>(R.id.ListOfPatientsBtn)
        patientsBtn.setOnClickListener{
            val patientListIntent = Intent(this, PatientListActivity::class.java)
            val nurseMetadata = getIntent().getStringExtra("nurseMetadata").toString()
            patientListIntent.putExtra("nurseMetadata", nurseMetadata)
            startActivity(patientListIntent)
        }



    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar, menu)
        return true
    }
}