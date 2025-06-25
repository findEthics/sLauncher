package com.example.slauncher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SLauncherApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply saved theme on app startup
        val sharedPreferences = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}