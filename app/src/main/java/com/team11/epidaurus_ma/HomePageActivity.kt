package com.team11.epidaurus_ma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Button
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class HomePageActivity : AppCompatActivity(), CoroutineScope {

    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ){
        install(Auth)
    }
    private var popupWindow: PopupWindow? = null
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        popupWindow?.dismiss()
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
        setContentView(R.layout.activity_home_page)

        val menuButton = findViewById<ImageView>(R.id.dashboardMenu)
        menuButton.setOnClickListener {
            showPopupMenu(menuButton)
        }

        //Logic for accessing the patient list activity
        val patientsBtn = findViewById<Button>(R.id.ListOfPatientsBtn)
        patientsBtn.setOnClickListener {
            val patientListIntent = Intent(this, PatientListActivity::class.java)
            val nurseMetadata = intent.getStringExtra("nurseMetadata").toString()
            patientListIntent.putExtra("nurseMetadata", nurseMetadata)
            Log.d("HomePageActivity", "Data: $nurseMetadata")
            startActivity(patientListIntent)
        }
    }

    private fun showPopupMenu(anchor: ImageView) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_dashboard, popupMenu.menu)
        val menuItems = popupMenu.menu.children.map {
            MenuItem(0, it.title.toString())
        }

        val adapter = ToolbarAdapter(this, menuItems.toList())
        val listView = ListView(this).apply {
            this.adapter = adapter
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                handleMenuItemClick(popupMenu.menu.getItem(position).itemId)
            }
        }
        popupWindow = PopupWindow(
            listView, 400, 350,true).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ContextCompat.getDrawable(this@HomePageActivity, R.drawable.toolbar_box))
            showAsDropDown(anchor, 0, 0)
        }
    }

    private fun handleMenuItemClick(itemId: Int) {
        popupWindow?.dismiss()
        when (itemId) {
            R.id.profileBtn -> { // Profile clicked
                Log.d("HomePageActivity", "Profile menu item clicked")
                val profileIntent = Intent(this, ProfileActivity::class.java)
                val nurseMetadata = intent.getStringExtra("nurseMetadata").toString()
                profileIntent.putExtra("nurseMetadata", nurseMetadata)
                Log.d("HomePageActivity", "Sending Data to ProfileActivity: $nurseMetadata")
                startActivity(profileIntent)
            }
            R.id.settingsBtn -> { // Settings clicked
                Log.d("HomePageActivity", "Settings menu item clicked")
                //TODO: make settings for the app
            }
            R.id.signOutBtn -> { // Logout clicked
                logoutUser(supabase)
                Log.d("HomePageActivity", "Sign out menu item clicked")
            }
            else -> {
                Log.e("HomePageActivity", "Unknown menu item clicked")
            }
        }
    }

    private fun logoutUser(supabase: SupabaseClient) {
        launch {
            try {
                supabase.auth.signOut()
                Log.d("HomePageActivity", "Logout successful")
                redirectToLoginScreen()
            } catch (e: Exception) {
                Log.e("HomePageActivity", "Error during logout: ${e.message}")
                Toast.makeText(this@HomePageActivity, "Error occurred!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToLoginScreen() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        Log.d("HomePageActivity", "Redirecting to MainActivity")
        startActivity(mainIntent)
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
    }
}