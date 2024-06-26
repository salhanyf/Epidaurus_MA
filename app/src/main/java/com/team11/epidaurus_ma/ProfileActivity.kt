package com.team11.epidaurus_ma

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Button
import android.widget.GridView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class ProfileActivity : AppCompatActivity(), CoroutineScope {

    private val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU"
    ){
        install(Auth)
    }
    companion object {
        private const val PREFS_NAME = "ProfilePrefs"
        private const val AVATAR_KEY = "avatar_resource_id"
    }
    private var selectedAvatarResId: Int = R.drawable.avatar_4
    private var originalName: String? = null
    private var originalDep: String? = null
    private var originalFloor: String? = null
    private var departmentId: String = "0"
    private var floorId: String = "0"
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
    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val avatarImage = findViewById<ImageView>(R.id.nurseAvatar)
        val nameEditText = findViewById<EditText>(R.id.NameEditText)
        val departmentSpinner = findViewById<Spinner>(R.id.departmentEditText)
        val floorSpinner = findViewById<Spinner>(R.id.floorEditText)
        val changeAvatarBtn = findViewById<Button>(R.id.changeAvatarButton)
        val editBtn = findViewById<Button>(R.id.editButton)
        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        val saveBtn = findViewById<Button>(R.id.saveButton)

        launch {
            fetchUserData(supabase, nameEditText, departmentSpinner, floorSpinner)
        }

        // Spinners initially disabled
        departmentSpinner.isEnabled = false
        floorSpinner.isEnabled = false
        setupSpinner(departmentSpinner, R.array.department_array, "Select Department") { selected, position ->
            departmentId = when (selected) {
                "Cardiology" -> "Cardiology"
                "Neurology" -> "Neurology"
                "Gastroenterology" -> "Gastroenterology"
                "Pulmonology" -> "Pulmonology"
                else -> "0"
            }
        }
        setupSpinner(floorSpinner, R.array.floor_array, "Select Floor") { selected, position ->
            floorId = when (selected) {
                "Floor 1" -> "1"
                "Floor 2" -> "2"
                "Floor 3" -> "3"
                else -> "0"
            }
        }

        // Load the avatar resource ID from SharedPreferences
        var sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        selectedAvatarResId = sharedPreferences.getInt(AVATAR_KEY, R.drawable.avatar_4)
        avatarImage.setImageResource(selectedAvatarResId)

        editBtn.setOnClickListener{
            changeAvatarBtn.visibility = View.VISIBLE
            nameEditText.isEnabled = true
            nameEditText.setBackgroundResource(R.drawable.outlined_box_edit)
            departmentSpinner.isEnabled = true
            departmentSpinner.setBackgroundResource(R.drawable.outlined_box_edit)
            floorSpinner.isEnabled = true
            floorSpinner.setBackgroundResource(R.drawable.outlined_box_edit)

            editBtn.visibility = View.GONE
            cancelBtn.visibility = View.VISIBLE
            saveBtn.visibility = View.VISIBLE

            nameEditText.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        cancelBtn.setOnClickListener{
            changeAvatarBtn.visibility = View.GONE
            nameEditText.isEnabled = false
            nameEditText.setBackgroundResource(R.drawable.outlined_box_green)
            departmentSpinner.isEnabled = false
            departmentSpinner.setBackgroundResource(R.drawable.outlined_box_green)
            floorSpinner.isEnabled = false
            floorSpinner.setBackgroundResource(R.drawable.outlined_box_green)

            editBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            saveBtn.visibility = View.GONE
        }

        saveBtn.setOnClickListener{
            // Save the selected avatar resource ID
            changeAvatarBtn.visibility = View.GONE
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt(AVATAR_KEY, selectedAvatarResId)
                apply()
            }
            // Save the new name
            nameEditText.isEnabled = false
            nameEditText.setBackgroundResource(R.drawable.outlined_box_green)
            val currentName = nameEditText.text.toString()
            if (originalName != currentName) {
                updateNameInDatabase(supabase, currentName)
                nameEditText.setText(currentName)
            }

            // Save the new department
            departmentSpinner.isEnabled = false
            updateDepInDatabase(supabase, departmentId)
            departmentSpinner.setBackgroundResource(R.drawable.outlined_box_green)

            // Save the new floor
            floorSpinner.isEnabled = false
            updateFloorInDatabase(supabase, floorId)
            floorSpinner.setBackgroundResource(R.drawable.outlined_box_green)

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

    private suspend fun fetchUserData(
        supabase: SupabaseClient,
        nameEditText: EditText,
        departmentSpinner: Spinner,
        floorSpinner: Spinner
    ) {
        val metadata = supabase.auth.retrieveUserForCurrentSession()?.userMetadata.toString()
        var metadataJSON: JSONObject? = null
        metadataJSON = JSONObject(metadata)

        originalName = metadataJSON.get("name").toString()
        nameEditText.setText(originalName)

        originalDep = metadataJSON.getString("Department ID") ?: ""
        val departmentIndex = resources.getStringArray(R.array.department_array).indexOf(originalDep)
        departmentSpinner.setSelection(max(departmentIndex, 0))

        originalFloor = metadataJSON.get("Floor ID").toString()
        val floorValueFromJson = metadataJSON.getString("Floor ID") ?: ""
        val floorValue = if (floorValueFromJson.all { it.isDigit() }) "Floor $floorValueFromJson" else floorValueFromJson
        val floorIndex = resources.getStringArray(R.array.floor_array).indexOf(floorValue)
        floorSpinner.setSelection(max(floorIndex, 0))
        Log.d("ProfileActivity", "Department: $originalDep , Floor: $originalFloor")
    }

    private fun updateNameInDatabase(supabase: SupabaseClient, newName: String) {
        launch {
            try {
                val user = supabase.auth.modifyUser {
                    data {
                        put("name", newName)
                    }
                }
                // Update successful
                Log.d("ProfileActivity", "Name updated to: $newName")
                Toast.makeText(this@ProfileActivity, "Name updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error updating name", e)
                Toast.makeText(this@ProfileActivity, "Error updating name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDepInDatabase(supabase: SupabaseClient, newDep: String) {
        launch {
            try {
                val user = supabase.auth.modifyUser {
                    data {
                        put("Department ID", newDep)
                    }
                }
                // Update successful
                Log.d("ProfileActivity", "Department updated to: $newDep")
                //Toast.makeText(this@ProfileActivity, "Department updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error updating department", e)
                //Toast.makeText(this@ProfileActivity, "Error updating department", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFloorInDatabase(supabase: SupabaseClient, newFloor: String) {
        launch {
            try {
                val user = supabase.auth.modifyUser {
                    data {
                        put("Floor ID",newFloor)
                    }
                }
                // Update successful
                Log.d("ProfileActivity", "Floor updated to: $newFloor")
                //Toast.makeText(this@ProfileActivity, "Floor updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error updating floor", e)
                //Toast.makeText(this@ProfileActivity, "Error updating floor", Toast.LENGTH_SHORT).show()
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

    private fun setupSpinner(spinner: Spinner, arrayId: Int, hint: String, onSelect: (String, Int) -> Unit) {
        val items = resources.getStringArray(arrayId).toList()
        val adapter = DropdownAdapter(this, R.layout.dropdown_menu, items, hint)
        spinner.adapter = adapter
        spinner.dropDownVerticalOffset = 130
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
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
}
