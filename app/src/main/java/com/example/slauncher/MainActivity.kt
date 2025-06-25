package com.example.slauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var appIcons: MutableList<ImageView>
    private lateinit var selectedApps: MutableList<AppInfo?>
    private lateinit var appIconsContainer: GridLayout
    private var appCount: Int = 6
    private lateinit var installedApps: List<AppInfo>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var timeDisplay: TextView
    private lateinit var batteryDisplay: TextView
    private lateinit var timeUpdateHandler: Handler
    private lateinit var batteryReceiver: BroadcastReceiver
    
    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_APP_PREFIX = "selected_app_"
        private const val KEY_APP_COUNT = "app_count"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        applyTheme()
        
        setContentView(R.layout.activity_main)
        
        appCount = sharedPreferences.getInt(KEY_APP_COUNT, 6)
        initializeViews()
        loadInstalledApps()
        loadSavedApps()
        setupClickListeners()
        setupAllAppsButton()
        setupTimeAndBattery()
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
        
        // Refresh time and battery when resuming
        if (::timeDisplay.isInitialized) {
            updateTime()
            updateBattery()
        }
    }
    
    private fun configureGridLayout() {
        val (columns, rows) = when (appCount) {
            2 -> Pair(1, 2)  // 1x2 grid
            4 -> Pair(2, 2)  // 2x2 grid
            6 -> Pair(2, 3)  // 2x3 grid
            8 -> Pair(2, 4)  // 2x4 grid
            else -> Pair(2, 3) // Default to 2x3
        }
        
        appIconsContainer.columnCount = columns
        appIconsContainer.rowCount = rows
    }
    
    private fun openClockApp() {
        // Try to open the default clock/alarm app
        // Common clock app package names
        val clockPackages = listOf(
            "com.google.android.deskclock",  // Google Clock
            "com.android.deskclock",         // AOSP Clock
            "com.samsung.android.app.clockpackage", // Samsung Clock
            "com.htc.android.worldclock",    // HTC Clock
            "com.sec.android.app.clockpackage" // Samsung variant
        )
        
        // Try each known clock package
        for (packageName in clockPackages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return
            }
        }
        
        // Fallback: Try to find any app with "clock" or "alarm" in the name
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val apps = pm.queryIntentActivities(mainIntent, 0)
        val clockApps = apps.filter { resolveInfo ->
            val appName = resolveInfo.loadLabel(pm).toString().lowercase()
            appName.contains("clock") || appName.contains("alarm") || appName.contains("time")
        }
        
        if (clockApps.isNotEmpty()) {
            val clockApp = clockApps.first()
            val launchIntent = Intent().apply {
                component = android.content.ComponentName(
                    clockApp.activityInfo.packageName,
                    clockApp.activityInfo.name
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(launchIntent)
        } else {
            // Final fallback: Open system settings for date/time
            try {
                val settingsIntent = Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
                startActivity(settingsIntent)
            } catch (e: Exception) {
                // If all else fails, do nothing
            }
        }
    }
    
    private fun applyTheme() {
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun initializeViews() {
        appIcons = mutableListOf()
        selectedApps = mutableListOf()
        appIconsContainer = findViewById<GridLayout>(R.id.app_names_container)
        
        // Clear existing views
        appIconsContainer.removeAllViews()
        
        // Configure grid layout based on app count
        configureGridLayout()
        
        // Create dynamic ImageViews based on app count
        for (i in 0 until appCount) {
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = (80 * resources.displayMetrics.density).toInt()
                    height = (80 * resources.displayMetrics.density).toInt()
                    setMargins(
                        (16 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt()
                    )
                }
                // Use theme-aware background
                val typedArray2 = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
                background = typedArray2.getDrawable(0)
                typedArray2.recycle()
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(
                    (8 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt()
                )
                setImageResource(android.R.drawable.sym_def_app_icon)
            }
            
            appIcons.add(imageView)
            selectedApps.add(null)
            appIconsContainer.addView(imageView)
        }
        
        updateDateDisplay()
        
        // Initialize time and battery displays
        timeDisplay = findViewById(R.id.time_display)
        batteryDisplay = findViewById(R.id.battery_display)
        
        // Set click listener for time display
        timeDisplay.setOnClickListener {
            openClockApp()
        }
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
        val appNamesList = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select App")
            .setItems(appNamesList) { _, which ->
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
                    appIcons[i].setImageDrawable(app.icon)
                }
            }
        }
    }
    
    private fun setupTimeAndBattery() {
        // Initialize handler for time updates
        timeUpdateHandler = Handler(Looper.getMainLooper())
        
        // Update time immediately and then every minute
        updateTime()
        startTimeUpdates()
        
        // Setup battery monitoring
        setupBatteryMonitoring()
        updateBattery()
    }
    
    private fun updateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        timeDisplay.text = currentTime
    }
    
    private fun startTimeUpdates() {
        val updateTimeRunnable = object : Runnable {
            override fun run() {
                updateTime()
                timeUpdateHandler.postDelayed(this, 60000) // Update every minute
            }
        }
        timeUpdateHandler.postDelayed(updateTimeRunnable, 60000)
    }
    
    private fun setupBatteryMonitoring() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateBattery()
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }
    
    private fun updateBattery() {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level != -1 && scale != -1) {
            val batteryPct = (level * 100 / scale)
            batteryDisplay.text = "Battery: $batteryPct%"
        } else {
            batteryDisplay.text = "Battery: --"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up
        if (::timeUpdateHandler.isInitialized) {
            timeUpdateHandler.removeCallbacksAndMessages(null)
        }
        
        if (::batteryReceiver.isInitialized) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
    }
    
    private fun saveAppToPreferences(position: Int, packageName: String) {
        sharedPreferences.edit()
            .putString("$KEY_APP_PREFIX$position", packageName)
            .apply()
    }
}