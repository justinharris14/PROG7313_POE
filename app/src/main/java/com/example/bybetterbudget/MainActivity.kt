package com.example.bybetterbudget

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check authentication status
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is logged in - go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User not logged in - go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish() // Close MainActivity
    }
}