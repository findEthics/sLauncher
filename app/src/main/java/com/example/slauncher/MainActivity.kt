package com.example.slauncher

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var appIcons: Array<ImageView>
    private val selectedApps = Array<AppInfo?>(6) { null }
    private lateinit var installedApps: List<AppInfo>
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_APP_PREFIX = "selected_app_"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        initializeViews()
        loadInstalledApps()
        loadSavedApps()
        setupClickListeners()
        setupAllAppsButton()
    }
    
    private fun initializeViews() {
        appIcons = arrayOf(
            findViewById(R.id.app_icon_1),
            findViewById(R.id.app_icon_2),
            findViewById(R.id.app_icon_3),
            findViewById(R.id.app_icon_4),
            findViewById(R.id.app_icon_5),
            findViewById(R.id.app_icon_6)
        )
        
        updateDateDisplay()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val dateDisplay = findViewById<TextView>(R.id.date_display)
        dateDisplay.text = currentDate
        
        dateDisplay.setOnClickListener {
            openCalendarApp()
        }
    }
    
    private fun openCalendarApp() {
        // First try to open Mudita calendar
        val muditaIntent = packageManager.getLaunchIntentForPackage("com.mudita.calendar")
        if (muditaIntent != null) {
            startActivity(muditaIntent)
            return
        }
        
        // Fall back to default calendar app
        val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
        }
        
        if (calendarIntent.resolveActivity(packageManager) != null) {
            startActivity(calendarIntent)
        } else {
            // Last resort - try opening calendar with date view
            val dateIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("content://com.android.calendar/time")
            }
            if (dateIntent.resolveActivity(packageManager) != null) {
                startActivity(dateIntent)
            }
        }
    }
    
    private fun loadInstalledApps() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val apps = pm.queryIntentActivities(mainIntent, 0)
        installedApps = apps.map { resolveInfo ->
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(pm).toString(),
                icon = resolveInfo.loadIcon(pm)
            )
        }.sortedBy { it.appName }
    }
    
    private fun setupClickListeners() {
        appIcons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                if (selectedApps[index] == null) {
                    showAppSelectionDialog(index)
                } else {
                    launchApp(selectedApps[index]!!)
                }
            }
            
            imageView.setOnLongClickListener {
                showAppSelectionDialog(index)
                true
            }
        }
    }
    
    private fun showAppSelectionDialog(position: Int) {
        val appNames = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select App")
            .setItems(appNames) { _, which ->
                val selectedApp = installedApps[which]
                selectedApps[position] = selectedApp
                appIcons[position].setImageDrawable(selectedApp.icon)
                saveAppToPreferences(position, selectedApp.packageName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun launchApp(appInfo: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }
    
    private fun setupAllAppsButton() {
        findViewById<ImageButton>(R.id.all_apps_button).setOnClickListener {
            showAllAppsDialog()
        }
    }
    
    private fun showAllAppsDialog() {
        val appNames = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("All Installed Apps")
            .setItems(appNames) { _, which ->
                launchApp(installedApps[which])
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun loadSavedApps() {
        for (i in 0 until 6) {
            val packageName = sharedPreferences.getString("$KEY_APP_PREFIX$i", null)
            if (packageName != null) {
                val app = installedApps.find { it.packageName == packageName }
                if (app != null) {
                    selectedApps[i] = app
                    appIcons[i].setImageDrawable(app.icon)
                }
            }
        }
    }
    
    private fun saveAppToPreferences(position: Int, packageName: String) {
        sharedPreferences.edit()
            .putString("$KEY_APP_PREFIX$position", packageName)
            .apply()
    }
}