package com.example.slauncher

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), 
    GridManager.GridUpdateListener,
    SystemInfoManager.SystemInfoListener,
    GestureManager.GestureListener {
    
    private lateinit var appSelectionManager: AppSelectionManager
    private lateinit var gridManager: GridManager
    private lateinit var systemInfoManager: SystemInfoManager
    private lateinit var gestureManager: GestureManager
    private lateinit var iconCacheManager: IconCacheManager
    private lateinit var appGridRecycler: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        initializeManagers()
        applyTheme()
        
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupAllAppsButton()
    }
    
    override fun onResume() {
        super.onResume()
        
        val newAppCount = appSelectionManager.getAppCount()
        appSelectionManager.updateAppCount(newAppCount)
        
        refreshGrid()
    }
    
    private fun initializeManagers() {
        appSelectionManager = AppSelectionManager(this)
        gridManager = GridManager(this, appSelectionManager)
        systemInfoManager = SystemInfoManager(this)
        gestureManager = GestureManager(this, this)
        iconCacheManager = IconCacheManager.getInstance(this)
        
        gridManager.setGridUpdateListener(this)
        systemInfoManager.setSystemInfoListener(this)
    }
    
    override fun onTimeClicked() {
        systemInfoManager.openClockApp()
    }
    
    override fun onDateClicked() {
        systemInfoManager.openCalendarApp()
    }
    
    override fun onAppSelected(position: Int, appInfo: AppInfo) {
        appSelectionManager.selectApp(position, appInfo)
        gridManager.updateAppIcon(position, appInfo)
    }
    
    override fun onAppLaunched(appInfo: AppInfo) {
        appSelectionManager.launchApp(appInfo)
    }
    
    override fun onSwipeUp() {
        // Launch AllAppsActivity when swiping up
        val intent = Intent(this, AllAppsActivity::class.java)
        startActivity(intent)
    }
    
    override fun onSwipeDown() {
        // Optional: Could implement something like closing all apps or settings
        // For now, do nothing
    }
    
    private fun applyTheme() {
        val isDarkMode = if (::appSelectionManager.isInitialized) {
            appSelectionManager.isDarkMode()
        } else {
            sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        }
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun initializeViews() {
        appGridRecycler = findViewById(R.id.app_grid_recycler)
        
        val appCount = appSelectionManager.getAppCount()
        appSelectionManager.initializeSelectedApps(appCount)
        
        refreshGrid()
        setupSystemInfo()
    }
    
    private fun refreshGrid() {
        val appCount = appSelectionManager.getAppCount()
        val selectedApps = appSelectionManager.getSelectedApps()
        gridManager.initializeGrid(appGridRecycler, appCount, selectedApps)
    }
    
    private fun setupSystemInfo() {
        val timeDisplay = findViewById<TextView>(R.id.time_display)
        val dateDisplay = findViewById<TextView>(R.id.date_display)
        val batteryDisplay = findViewById<TextView>(R.id.battery_display)
        val weatherDisplay = findViewById<TextView>(R.id.weather_display)
        
        systemInfoManager.initialize(timeDisplay, dateDisplay, batteryDisplay, weatherDisplay)
        
        // Setup gesture detection on the root layout
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        gestureManager.attachToView(rootLayout)
    }
    
    
    private fun setupAllAppsButton() {
        findViewById<ImageButton>(R.id.all_apps_button).setOnClickListener {
            val intent = Intent(this, AllAppsActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        systemInfoManager.destroy()
        gridManager.destroy()
    }
}