package com.example.myapplication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class DictionaryApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize database
            DictionaryDatabase.getDatabase(this)
            Log.d("DictionaryApp", "Database initialized successfully")
            
            // Apply saved theme preference
            applySavedTheme()
        } catch (e: Exception) {
            Log.e("DictionaryApp", "Error initializing database: ${e.message}", e)
        }
    }
    
    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Add any Xiaomi-specific initialization here if needed
    }
} 