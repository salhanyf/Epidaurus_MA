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

class SignUpActivity : AppCompatActivity(), CoroutineScope{
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Dropdown menu for departments
        val departments = resources.getStringArray(R.array.department_array).toMutableList()
        val departmentSpinner: Spinner = findViewById(R.id.departmentIdSignup)
        departmentSpinner.dropDownVerticalOffset = 130
        val departmentAdapter = DropdownAdapter(this, R.layout.dropdown_menu, departments, "Select Department")
        var departmentId: String = "0"
        departmentSpinner.adapter = departmentAdapter
        //TODO: @Farah please make it so that when I select an option on the drop down menu, that the option selected e.g. "Cardiology" replaces the Hint Text ("Select a department")
        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(parent?.getItemAtPosition(position).toString() /* <- @Farah, this is how you get the string of the option selected*/){
                    "Cardiology" -> departmentId = "1"
                    "Neurology" -> departmentId = "2"
                    "Gastroenterology" -> departmentId = "3"
                    "Pulmonology" -> departmentId = "4"
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                departmentId = "0"
            }
        }

        // Dropdown menu for departments
        val floors = resources.getStringArray(R.array.floor_array).toMutableList()
        val floorSpinner: Spinner = findViewById(R.id.floorIdSignup)
        floorSpinner.dropDownVerticalOffset = 130
        val floorAdapter = DropdownAdapter(this, R.layout.dropdown_menu, floors, "Select Floor")
        var floorId: String = "0"
        floorSpinner.adapter = floorAdapter
        //TODO: @Farah please make it so that when I select an option on the drop down menu, that the option selected e.g. "1" replaces the Hint Text ("Select a floor")
        floorSpinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(parent?.getItemAtPosition(position).toString() /* <- @Farah, this is how you get the string of the option selected*/){
                    "Floor 1" -> floorId = "1"
                    "Floor 2" -> floorId = "2"
                    "Floor 3" -> floorId = "3"
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                floorId = "0"
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
    //IMPORTANT: the user's metadata is not accessible on the Supabase Dashboard, it will be accessible at anytime with Kotlin code when the user signs in successfully in a valid session: https://www.restack.io/docs/supabase-knowledge-supabase-user-metadata
    /*
    val user = supabase.auth.retrieveUserForCurrentSession()
    //Or you can use the user from the current session:
    val user = supabase.auth.currentUserOrNull()
    val metadata = user?.userMetadata
    */

}