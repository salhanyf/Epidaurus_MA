package com.team11.epidaurus_ma

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Button
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    private val REQUEST_CODE_POST_NOTIFICATION_PERMISSION = 2001
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

        Intent(this, SupabaseListeningService::class.java).also { intent ->
            startForegroundService(intent)
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

        val profileBtn = findViewById<Button>(R.id.ProfileBtn)
        profileBtn.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            val nurseMetadata = intent.getStringExtra("nurseMetadata").toString()
            profileIntent.putExtra("nurseMetadata", nurseMetadata)
            Log.d("HomePageActivity", "Sending Data to ProfileActivity: $nurseMetadata")
            startActivity(profileIntent)
        }

        val notificationBtn: Button = findViewById(R.id.btnSendNotification)
        notificationBtn.setOnClickListener {
//            sendNotification()
        }
        checkAndRequestPermissions()
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
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
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

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request the POST_NOTIFICATIONS permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATION_PERMISSION)
        } else {
            // Permission has already been granted, start the foreground service
            startSupabaseListeningService()
        }
    }

    private fun startSupabaseListeningService() {
        val serviceIntent = Intent(this, SupabaseListeningService::class.java)
        startForegroundService(serviceIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, start the service
                startService(Intent(this, SupabaseListeningService::class.java))
            } else {
                Toast.makeText(this, "Permission denied. The app needs notification permission to function properly.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}