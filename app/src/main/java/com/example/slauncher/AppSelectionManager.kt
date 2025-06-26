package com.example.slauncher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class AppSelectionManager(private val context: Context) {
    
    private var installedApps: List<AppInfo> = emptyList()
    private var selectedApps: MutableList<AppInfo?> = mutableListOf()
    private var sharedPreferences: SharedPreferences
    private var iconCacheManager: IconCacheManager
    
    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_APP_PREFIX = "selected_app_"
        private const val KEY_APP_COUNT = "app_count"
        private const val KEY_DARK_MODE = "dark_mode"
    }
    
    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        iconCacheManager = IconCacheManager.getInstance(context)
        loadInstalledApps()
    }
    
    fun getInstalledApps(): List<AppInfo> = installedApps
    
    fun getSelectedApps(): List<AppInfo?> = selectedApps.toList()
    
    fun getAppCount(): Int = sharedPreferences.getInt(KEY_APP_COUNT, 6)
    
    fun isDarkMode(): Boolean = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    
    fun initializeSelectedApps(appCount: Int) {
        selectedApps.clear()
        repeat(appCount) { selectedApps.add(null) }
        loadSavedApps()
    }
    
    private fun loadInstalledApps() {
        val pm = context.packageManager
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
    
    private fun loadSavedApps() {
        for (i in selectedApps.indices) {
            val packageName = sharedPreferences.getString("$KEY_APP_PREFIX$i", null)
            if (packageName != null) {
                val app = installedApps.find { it.packageName == packageName }
                if (app != null) {
                    selectedApps[i] = app
                }
            }
        }
    }
    
    fun selectApp(position: Int, appInfo: AppInfo) {
        if (position < selectedApps.size) {
            selectedApps[position] = appInfo
            saveAppToPreferences(position, appInfo.packageName)
        }
    }
    
    fun launchApp(appInfo: AppInfo): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getSelectedApp(position: Int): AppInfo? {
        return if (position < selectedApps.size) selectedApps[position] else null
    }
    
    private fun saveAppToPreferences(position: Int, packageName: String) {
        sharedPreferences.edit()
            .putString("$KEY_APP_PREFIX$position", packageName)
            .apply()
    }
    
    fun refreshInstalledApps() {
        loadInstalledApps()
        // Preload icons for better performance
        iconCacheManager.preloadIcons(installedApps)
    }
    
    fun updateAppCount(newAppCount: Int) {
        val currentSize = selectedApps.size
        
        if (newAppCount > currentSize) {
            repeat(newAppCount - currentSize) { selectedApps.add(null) }
        } else if (newAppCount < currentSize) {
            selectedApps = selectedApps.take(newAppCount).toMutableList()
        }
        
        loadSavedApps()
    }
}