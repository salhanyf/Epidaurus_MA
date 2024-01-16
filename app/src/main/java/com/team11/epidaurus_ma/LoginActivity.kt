package com.team11.epidaurus_ma

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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

        // Sign up redirect btn
        val signUpBtn = findViewById<Button>(R.id.signUpRedirect)
        signUpBtn.setOnClickListener{
            val signUnIntent = Intent (this, SignUpActivity::class.java)
            startActivity(signUnIntent)
        }

        val supabase = createSupabaseClient(
            "https://faafgdjvgcpjbchmbmhg.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU"
        ){
            install(Auth)
        }

        val signInBtn = findViewById<Button>(R.id.SignInButton)
        signInBtn.setOnClickListener{
            val emailET = findViewById<EditText>(R.id.emailSignIn)
            val pswET = findViewById<EditText>(R.id.passwordSignIn)
            val emailStr = emailET.text.toString()
            val pswStr = pswET.text.toString()
            if(emailStr == "" || pswStr == ""||emailStr == null || pswStr == null){
               Toast.makeText(this, "Credentials are empty", Toast.LENGTH_SHORT).show()
            }else{
                launch{
                    signIn(supabase,emailStr,pswStr)
                }
            }

        }
    }

    suspend fun signIn(supabase: SupabaseClient, emailStr:String?, pswStr:String?){
        supabase.auth.signInWith(Email){
            if (emailStr != null) {
                email = emailStr
            }
            if (pswStr != null) {
                password = pswStr
            }
        }
        supabase.auth.sessionStatus.collect{
            when(it){
                is SessionStatus.Authenticated -> Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show()
                SessionStatus.LoadingFromStorage -> Toast.makeText(this, "Loading from Storage", Toast.LENGTH_SHORT).show()
                SessionStatus.NetworkError -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
                SessionStatus.NotAuthenticated -> Toast.makeText(this, "Sign in failure", Toast.LENGTH_SHORT).show()
            }
        }
    }
}