package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth

class MainActivity : AppCompatActivity() {
    val supabase = createSupabaseClient(
        "https://faafgdjvgcpjbchmbmhg.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhYWZnZGp2Z2NwamJjaG1ibWhnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDM2MTg4NzAsImV4cCI6MjAxOTE5NDg3MH0.aqg8lSD7tQrWhXjfZi7OiRJOEF1ArG-wdRd9KauvZPU"
    ){
        install(Auth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkUserSession()

        val signUpBtn = findViewById<Button>(R.id.SignUpBtn)
        val signInBtn = findViewById<Button>(R.id.SignInBtn)

        signUpBtn.setOnClickListener{
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }

        signInBtn.setOnClickListener{
            val signInIntent = Intent (this, LoginActivity::class.java)
            startActivity(signInIntent)
        }
    }

    private fun checkUserSession() {
        val session = supabase.auth.currentSessionOrNull()

        if (session != null) {
            val homeIntent = Intent(this, HomePageActivity::class.java)
            startActivity(homeIntent)
            finish()
        }
    }
}