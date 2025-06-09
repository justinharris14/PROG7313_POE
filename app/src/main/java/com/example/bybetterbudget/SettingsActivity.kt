// File: app/src/main/java/com/example/bybetterbudget/SettingsActivity.kt

package com.example.bybetterbudget

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.bybetterbudget.utils.AppPreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: Switch
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnLogout: Button
    private lateinit var btnBackHome: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppPreferences.applyTheme(this)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        switchDarkMode  = findViewById(R.id.switchDarkMode)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        btnLogout       = findViewById(R.id.btnLogout)
        btnBackHome     = findViewById(R.id.btnBackHome)


        switchDarkMode.isChecked = AppPreferences.isDarkModeEnabled(this)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setDarkModeEnabled(this, isChecked)
            recreate()
        }


        val currencies = listOf("ZAR", "EUR", "USD")
        spinnerCurrency.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, currencies
        )

        val current = AppPreferences.getCurrency(this)
        val pos = currencies.indexOf(current).takeIf { it >= 0 } ?: 0
        spinnerCurrency.setSelection(pos)
        // on change
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                AppPreferences.setCurrency(this@SettingsActivity, currencies[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        btnBackHome.setOnClickListener {
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(this)
            }
            finish()
        }
    }
}
