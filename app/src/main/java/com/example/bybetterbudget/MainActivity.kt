package com.example.bybetterbudget

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class MainActivity : AppCompatActivity() {
    private val TAG = "FirestoreTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Firestore connection test ---
        val db: FirebaseFirestore = Firebase.firestore
        val testData = hashMapOf(
            "connected"  to true,
            "checkedAt" to System.currentTimeMillis()
        )
        db.collection("connectionTests")
            .document("testDoc")
            .set(testData)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Firestore write succeeded")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Firestore write failed", e)
            }
    }
}
