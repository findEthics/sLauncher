package com.example.slauncher

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var appNames: MutableList<TextView>
    private lateinit var selectedApps: MutableList<AppInfo?>
    private lateinit var appNamesContainer: LinearLayout
    private var appCount: Int = 6
    private lateinit var installedApps: List<AppInfo>
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_APP_PREFIX = "selected_app_"
        private const val KEY_APP_COUNT = "app_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        appCount = sharedPreferences.getInt(KEY_APP_COUNT, 6)
        initializeViews()
        loadInstalledApps()
        loadSavedApps()
        setupClickListeners()
        setupAllAppsButton()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check if app count has changed and refresh if needed
        val newAppCount = sharedPreferences.getInt(KEY_APP_COUNT, 6)
        if (newAppCount != appCount) {
            appCount = newAppCount
            initializeViews()
            loadSavedApps()
            setupClickListeners()
        }
    }
    
    private fun initializeViews() {
        appNames = mutableListOf()
        selectedApps = mutableListOf()
        appNamesContainer = findViewById<LinearLayout>(R.id.app_names_container)
        
        // Clear existing views
        appNamesContainer.removeAllViews()
        
        // Create dynamic TextViews based on app count
        for (i in 0 until appCount) {
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(
                        (8 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt()
                    )
                }
                background = getDrawable(android.R.drawable.list_selector_background)
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(
                    (16 * resources.displayMetrics.density).toInt(),
                    0,
                    (16 * resources.displayMetrics.density).toInt(),
                    0
                )
                text = "Select App"
                textSize = 16f
                setTextColor(getColor(android.R.color.primary_text_light))
            }
            
            appNames.add(textView)
            selectedApps.add(null)
            appNamesContainer.addView(textView)
        }
        
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
        appNames.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                if (selectedApps[index] == null) {
                    showAppSelectionDialog(index)
                } else {
                    launchApp(selectedApps[index]!!)
                }
            }
            
            textView.setOnLongClickListener {
                showAppSelectionDialog(index)
                true
            }
        }
    }
    
    private fun showAppSelectionDialog(position: Int) {
        val appNamesList = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select App")
            .setItems(appNamesList) { _, which ->
                val selectedApp = installedApps[which]
                selectedApps[position] = selectedApp
                appNames[position].text = selectedApp.appName
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
            val intent = Intent(this, AllAppsActivity::class.java)
            startActivity(intent)
        }
    }
    
    
    private fun loadSavedApps() {
        for (i in 0 until appCount) {
            val packageName = sharedPreferences.getString("$KEY_APP_PREFIX$i", null)
            if (packageName != null) {
                val app = installedApps.find { it.packageName == packageName }
                if (app != null) {
                    selectedApps[i] = app
                    appNames[i].text = app.appName
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