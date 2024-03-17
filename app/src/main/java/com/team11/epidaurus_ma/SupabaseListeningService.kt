package com.team11.epidaurus_ma
import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.coroutines.CoroutineContext

class SupabaseListeningService : Service(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private var previousFallDetected: Int? = null

    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
    ) {
        install(Auth)
        install(Postgrest)
    }
    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel the coroutine job when the service is destroyed
        launch{
            supabase.close()
        }
    }

    private fun createNotificationChannel() {
        val name = "Urgent Alerts"
        val descriptionText = "Vitals Changes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val vitalsChannel = NotificationChannel(CHANNEL_ID, name, importance)
        vitalsChannel.description = descriptionText

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(vitalsChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.epidaurus_logo)
            .setContentTitle("Listening Service Running")
            .setContentText("Listening for vitals updates...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()
        startForeground(1, notification)

        // Here, you would set up your Supabase subscription or other logic
        //subscribeToVitalsChanges()
        startPeriodicCheck()
        return START_STICKY
    }

    private fun startPeriodicCheck() {
        // Cancel any existing checks to avoid duplication
        coroutineContext.cancelChildren()

        launch {
            while (isActive) { // Loop as long as the coroutine is active
                checkForFall()
                delay(5000L) // Wait for 5 seconds before checking again
            }
        }
    }

    // Add a function to trigger checking the database and possibly showing a notification
    private suspend fun checkForFall() {
        try {
            val value = supabaseFallFetch()
            // Trigger the notification only if fallDetected changed from 0 to 1
            if (value != null && value.fallDetected == 1 && (previousFallDetected == null || previousFallDetected == 0)) {
                triggerNotificationForFallDetected(value)
            }
            previousFallDetected = value?.fallDetected
        } catch (e: Exception) {
            Log.e("SupabaseListeningService", "Error checking for fall: ${e.message}")
        }
    }

    private suspend fun supabaseFallFetch(): VitalsResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase.from("ESP32FallDetection").select(){}.decodeSingle<VitalsResponse>()
                response
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null in case of error
            }
        }
    }

    private fun triggerNotificationForFallDetected(value: VitalsResponse) {

        val fullScreenIntent = Intent(this, AlertActivity::class.java).apply {
            putExtra("deviceId", value.id)
            putExtra("patientId", value.patientID)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE)

        // Building the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.epidaurus_logo)
            .setContentTitle("Fall Detected Alert")
            .setContentText("Immediate attention required.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)

        val serviceContext = this
        // Show the notification
        with(NotificationManagerCompat.from(this)) {
            // Now 'this' refers to NotificationManagerCompat, and 'serviceContext' is the Service
            if (ActivityCompat.checkSelfPermission(serviceContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(0, notificationBuilder.build())
        }
    }
}
