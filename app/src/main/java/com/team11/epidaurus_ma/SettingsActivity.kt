package com.team11.epidaurus_ma

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.hide()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Navigate to ProfileActivity when Edit Profile is clicked
            findPreference<Preference>("edit_profile")?.setOnPreferenceClickListener {
                startActivity(Intent(activity, ProfileActivity::class.java))
                true
            }

            // Handle notification switch
            findPreference<SwitchPreferenceCompat>("enable_notifications")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    val enabled = newValue as Boolean
                    // Handle the change
                    Toast.makeText(context, "Notifications are ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            findPreference<Preference>("app_notification")?.setOnPreferenceClickListener {
                val intent = Intent().apply {
                    when {
                        true -> {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                        }
                        else -> {
                            action = "android.settings.APP_NOTIFICATION_SETTINGS"
                            putExtra("app_package", context?.packageName)
                            putExtra("app_uid", context?.applicationInfo?.uid)
                        }
                    }
                }
                startActivity(intent)
                true
            }
        }
    }
}