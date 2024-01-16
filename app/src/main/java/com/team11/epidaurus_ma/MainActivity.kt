package com.team11.epidaurus_ma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
}