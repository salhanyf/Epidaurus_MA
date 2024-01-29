package com.team11.epidaurus_ma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class LoginActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailET = findViewById<EditText>(R.id.emailSignIn)
        val pswET = findViewById<EditText>(R.id.passwordSignIn)
        val signInBtn = findViewById<Button>(R.id.SignInButton)
        val signUpBtn = findViewById<Button>(R.id.signUpRedirect)
        val errorMessageTV = findViewById<TextView>(R.id.errorMessage)

        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU",
        ){
            install(Auth)
        }

        // Sign up redirect btn
        signUpBtn.setOnClickListener{
            val signUnIntent = Intent (this, SignUpActivity::class.java)
            startActivity(signUnIntent)
        }

        // Sign in btn
        signInBtn.setOnClickListener{
            val emailStr = emailET.text.toString()
            val pswStr = pswET.text.toString()
            emailET.setBackgroundResource(R.drawable.outlined_box_green)
            pswET.setBackgroundResource(R.drawable.outlined_box_green)
            errorMessageTV.visibility= View.GONE

            var isValid = true

            if(emailStr.isEmpty()){
                emailET.setBackgroundResource(R.drawable.outlined_box_red)
                emailET.error = "Email must not be empty"
                isValid = false
            }

            if (pswStr.isEmpty()) {
                pswET.setBackgroundResource(R.drawable.outlined_box_red)
                pswET.error = "Password must not be empty"
                isValid = false
            }

            if (!isValid) {
                // if one or both fields are empty

            } else{
                // if both fields are filled, attempt to sign in
                launch{
                    signIn(supabase,emailStr,pswStr)
                }
            }
        }
    }

    private suspend fun signIn(supabase: SupabaseClient, emailStr:String?, pswStr:String?){
        try {
            supabase.auth.signInWith(Email) {
                if (emailStr != null) {
                    this.email = emailStr
                }
                if (pswStr != null) {
                    this.password = pswStr
                }
            }
            supabase.auth.sessionStatus.collect{
                when(it){
                    is SessionStatus.Authenticated -> {
                        Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show()
                        val metadata = supabase.auth.retrieveUserForCurrentSession()?.userMetadata
                        val homePageIntent = Intent(this, HomePageActivity::class.java)
                        homePageIntent.putExtra("nurseMetadata", metadata.toString())
                        supabase.close()
                        startActivity(homePageIntent)
                    }
                    SessionStatus.LoadingFromStorage -> {
                        Toast.makeText(this, "Loading from Storage", Toast.LENGTH_SHORT).show()
                        supabase.close()
                    }
                    SessionStatus.NetworkError -> {
                        Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
                        supabase.close()
                    }
                    SessionStatus.NotAuthenticated -> {
                        Toast.makeText(this, "Authenticated Failed", Toast.LENGTH_SHORT).show()
                        supabase.close()
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessageTV = findViewById<TextView>(R.id.errorMessage)
            errorMessageTV.visibility= View.VISIBLE
            errorMessageTV.text = getString(R.string.invalid_email_pass)
            e.printStackTrace()
        }
    }
}