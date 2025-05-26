// app/src/main/java/com/example/bybetterbudget/MyApp.kt
package com.example.bybetterbudget

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // This initializes Firebase once for your entire app
        FirebaseApp.initializeApp(this)
    }
}
