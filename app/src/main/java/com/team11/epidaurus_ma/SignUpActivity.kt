package com.team11.epidaurus_ma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

class SignUpActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var departmentId: String = "0"
    private var floorId: String = "0"

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Dropdown menu for departments
        setupSpinner(findViewById(R.id.departmentIdSignup), R.array.department_array, "Select Department") { selected, position ->
            departmentId = when (selected) {
                "Cardiology" -> "Cardiology"
                "Neurology" -> "Neurology"
                "Gastroenterology" -> "Gastroenterology"
                "Pulmonology" -> "Pulmonology"
                else -> "0"
            }
        }
        // Dropdown menu for floors
        setupSpinner(findViewById(R.id.floorIdSignup), R.array.floor_array, "Select Floor") { selected, position ->
            floorId = when (selected) {
                "Floor 1" -> "1"
                "Floor 2" -> "2"
                "Floor 3" -> "3"
                else -> "0"
            }
        }

        // Login redirect btn
        val signInBtn = findViewById<Button>(R.id.loginRedirect)
        signInBtn.setOnClickListener{
            val signInIntent = Intent (this, LoginActivity::class.java)
            startActivity(signInIntent)
        }

        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU"
        ){
            install(Auth)
        }
        val createAcctBtn = findViewById<Button>(R.id.createAccountButton)
        createAcctBtn.setOnClickListener{
            val map = getAllEditTextValues()
            if (map["name"] == "" ||map["email"] == "" ||map["password"] == "" || map["passwordConfirm"] == "" || departmentId == "0" ||floorId == "0"){
                Toast.makeText(this, "All fields must be filled to register", Toast.LENGTH_SHORT).show()
            }else if(map["password"] != map["passwordConfirm"]) {
                Toast.makeText(this, "You did not enter the same password", Toast.LENGTH_SHORT).show()
            }else if ((map["password"]?.length ?: 0) < 6){
                Toast.makeText(this, "Passwords need to be 6 characters minimum", Toast.LENGTH_SHORT).show()
            }else{
                launch {
                    signUp(supabase,
                        map["name"],
                        map["email"],
                        map["password"],
                        departmentId,
                        floorId
                    )
                }
            }
        }
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

    private fun getAllEditTextValues(): Map<String, String> {
        val nameET = findViewById<EditText>(R.id.nameSignup)
        val emailET = findViewById<EditText>(R.id.emailSignup)
        val pswET = findViewById<EditText>(R.id.passwordSignup)
        val pswConfET = findViewById<EditText>(R.id.passwordConfirmSignup)

        val editTextMap = mutableMapOf<String, String>()

        editTextMap["name"] = nameET.text?.toString() ?: ""
        editTextMap["email"] = emailET.text?.toString() ?: ""
        editTextMap["password"] = pswET.text?.toString() ?: ""
        editTextMap["passwordConfirm"] = pswConfET.text?.toString() ?: ""

        return editTextMap
    }

    private suspend fun signUp(supabase:SupabaseClient, nameStr:String?, emailStr:String?, pswStr:String?, deptId:String?, floorId:String?){
        val user = supabase.auth.signUpWith(Email){
            if (emailStr != null) {
                email = emailStr
            }
            if (pswStr != null) {
                password = pswStr
            }
            data = buildJsonObject {
                put("name", nameStr)
                put("Department ID", deptId)
                put("Floor ID",floorId)
            }
        }
        if (user == null){
            Toast.makeText(this, "Registration success", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
        }
    }
}

/*
IMPORTANT: the user's metadata is not accessible on the Supabase Dashboard, it will be accessible at anytime with Kotlin code when the user signs in successfully in a valid session: https://www.restack.io/docs/supabase-knowledge-supabase-user-metadata
val user = supabase.auth.retrieveUserForCurrentSession()
//Or you can use the user from the current session:
val user = supabase.auth.currentUserOrNull()
val metadata = user?.userMetadata
*/
