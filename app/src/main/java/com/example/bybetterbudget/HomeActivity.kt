package com.example.bybetterbudget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()


        val userEmail = auth.currentUser?.email ?: "User"
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $userEmail!"


        findViewById<Button>(R.id.btnBudgeting).setOnClickListener {
            startActivity(Intent(this, BudgetingActivity::class.java))
        }

        findViewById<Button>(R.id.btnGoals).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        findViewById<Button>(R.id.btnReports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<Button>(R.id.btnProgress).setOnClickListener {
            startActivity(Intent(this, ProgressOverviewActivity::class.java))
        }



        findViewById<Button>(R.id.btnFeature2).setOnClickListener {
            startActivity(Intent(this, Feature2Activity::class.java))
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}