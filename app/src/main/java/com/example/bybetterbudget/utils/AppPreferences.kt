package com.example.bybetterbudget.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object AppPreferences {
    private const val PREFS_NAME = "bybetterbudget_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_CURRENCY = "currency"


    fun applyTheme(context: Context) {
        val enabled = isDarkModeEnabled(context)
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else        AppCompatDelegate.MODE_NIGHT_NO
        )
    }


    fun isDarkModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }


    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else        AppCompatDelegate.MODE_NIGHT_NO
        )
    }


    fun getCurrency(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "ZAR") ?: "ZAR"
    }


    fun setCurrency(context: Context, currency: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENCY, currency)
            .apply()
    }
}
