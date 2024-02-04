package com.team11.epidaurus_ma

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ProfileActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private const val PREFS_NAME = "ProfilePrefs"
        private const val AVATAR_KEY = "avatar_resource_id"
    }
    private var selectedAvatarResId: Int = R.drawable.avatar_4
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val avatarImage = findViewById<ImageView>(R.id.nurseAvatar)
        val nameEditText = findViewById<EditText>(R.id.NameEditText)
        val emailEditText = findViewById<EditText>(R.id.EmailEditText)
        val changeAvatarBtn = findViewById<Button>(R.id.changeAvatarButton)
        val editBtn = findViewById<Button>(R.id.editButton)
        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        val saveBtn = findViewById<Button>(R.id.saveButton)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Load the avatar resource ID from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        selectedAvatarResId = sharedPreferences.getInt(AVATAR_KEY, R.drawable.avatar_4)
        avatarImage.setImageResource(selectedAvatarResId)

        editBtn.setOnClickListener{
            nameEditText.isEnabled = true
            nameEditText.setBackgroundResource(R.drawable.outlined_box_edit)
            nameEditText.requestFocus()

            changeAvatarBtn.visibility = View.VISIBLE
            editBtn.visibility = View.GONE
            cancelBtn.visibility = View.VISIBLE
            saveBtn.visibility = View.VISIBLE

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        cancelBtn.setOnClickListener{
            nameEditText.isEnabled = false
            nameEditText.setBackgroundResource(R.drawable.outlined_box_green)

            changeAvatarBtn.visibility = View.GONE
            editBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            saveBtn.visibility = View.GONE
        }

        saveBtn.setOnClickListener{
            // Save the selected avatar resource ID
            val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt(AVATAR_KEY, selectedAvatarResId)
                apply()
            }
            nameEditText.isEnabled = false
            nameEditText.setBackgroundResource(R.drawable.outlined_box_green)
            changeAvatarBtn.visibility = View.GONE
            editBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            saveBtn.visibility = View.GONE
        }

        changeAvatarBtn.setOnClickListener {
            if (nameEditText.isEnabled) {
                showAvatarPickerDialog()
            }
        }
    }

    private fun showAvatarPickerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_picker, null)
        val gridView: GridView = dialogView.findViewById(R.id.avatarGridView)
        val avatarImage = findViewById<ImageView>(R.id.nurseAvatar)

        // Replace these with your actual drawable resource IDs
        val avatarList = listOf(R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3,
                                R.drawable.avatar_4, R.drawable.avatar_5, R.drawable.avatar_6,
                                R.drawable.avatar_7, R.drawable.avatar_8, R.drawable.avatar_9,
                                R.drawable.avatar_10, R.drawable.avatar_11, R.drawable.avatar_12)
        val adapter = AvatarAdapter(this, avatarList)
        gridView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose an Avatar")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedAvatarResId = avatarList[position]
            avatarImage.setImageResource(selectedAvatarResId)
            dialog.dismiss()
        }
        dialog.show()
    }
}

