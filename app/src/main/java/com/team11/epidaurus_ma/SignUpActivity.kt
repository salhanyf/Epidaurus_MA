package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU"
        ){
            install(Auth)
        }
        val createAcctBtn = findViewById<Button>(R.id.createAccountButton)
        createAcctBtn.setOnClickListener{
            val map = getAllEditTextValues()
            launch {
                signUp(supabase,
                    map["name"],
                    map["email"],
                    map["password"],
                    map["departmentId"],
                    map["roleId"],
                    map["floorId"]
                )
            }
        }
    }

    fun getAllEditTextValues(): Map<String, String> {
        val nameET = findViewById<EditText>(R.id.nameSignup)
        val dobET = findViewById<EditText>(R.id.DateOfBirthSignup)
        val emailET = findViewById<EditText>(R.id.emailSignup)
        val pswET = findViewById<EditText>(R.id.passwordSignup)
        val pswConfET = findViewById<EditText>(R.id.passwordConfirmSignup)
        val deptIdET = findViewById<EditText>(R.id.departmentIdSignup)
        val roleIdET = findViewById<EditText>(R.id.RoleIdSignup)
        val floorIdET = findViewById<EditText>(R.id.floorIdSignup)

        val editTextMap = mutableMapOf<String, String>()

        editTextMap["name"] = nameET.text?.toString() ?: ""
        editTextMap["dateOfBirth"] = dobET.text?.toString() ?: ""
        editTextMap["email"] = emailET.text?.toString() ?: ""
        editTextMap["password"] = pswET.text?.toString() ?: ""
        editTextMap["passwordConfirm"] = pswConfET.text?.toString() ?: ""
        editTextMap["departmentId"] = deptIdET.text?.toString() ?: ""
        editTextMap["roleId"] = roleIdET.text?.toString() ?: ""
        editTextMap["floorId"] = floorIdET.text?.toString() ?: ""

        return editTextMap
    }



    suspend fun signUp(supabase:SupabaseClient, nameStr:String?, emailStr:String?, pswStr:String?, deptId:String?, roleId:String?, floorId:String?){
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
                put("Role ID", roleId)
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

    //TODO: Create navigation link to SignUpActivity with "SIGN IN" TextView
    //TODO: Replace Date of Birth EditText with a DatePicker (or remove it altogether, TO BE DISCUSSED)
    //TODO: Add confirmation logic for password
    //TODO: Add restriction logic for name
    //TODO: Add restriction logic for password
}