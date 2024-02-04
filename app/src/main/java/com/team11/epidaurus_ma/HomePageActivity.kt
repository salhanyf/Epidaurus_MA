package com.team11.epidaurus_ma


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Button
import androidx.appcompat.view.ContextThemeWrapper
import kotlinx.coroutines.processNextEventInCurrentThread

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val menuButton = findViewById<ImageView>(R.id.dashboardMenu)
        menuButton.setOnClickListener {
            showPopupMenu(menuButton)
        }

        //Logic for accessing the patient list activity
        val patientsBtn = findViewById<Button>(R.id.ListOfPatientsBtn)
        patientsBtn.setOnClickListener{
            val patientListIntent = Intent(this, PatientListActivity::class.java)
            val nurseMetadata = getIntent().getStringExtra("nurseMetadata").toString()
            patientListIntent.putExtra("nurseMetadata", nurseMetadata)
            startActivity(patientListIntent)
        }
    }

    private fun showPopupMenu(anchor: ImageView) {
        val wrapper = ContextThemeWrapper(this, R.style.PopupMenuStyle)
        val popup = PopupMenu(wrapper, anchor)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_dashboard, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profileBtn -> {
                    // Handle menu_profile
                    val profileIntent = Intent (this, ProfileActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                R.id.settingsBtn -> {
                    // Handle menu_settings
                    true
                }
                R.id.signOutBtn -> {
                    // Handle sign out
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}