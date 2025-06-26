package com.example.slauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemInfoManager(private val context: Context) {
    
    private var timeUpdateHandler: Handler? = null
    private var batteryReceiver: BroadcastReceiver? = null
    private var weatherManager: WeatherManager? = null
    private var isActive = false
    
    interface SystemInfoListener {
        fun onTimeClicked()
        fun onDateClicked()
    }
    
    private var listener: SystemInfoListener? = null
    
    fun setSystemInfoListener(listener: SystemInfoListener) {
        this.listener = listener
    }
    
    fun initialize(
        timeDisplay: TextView,
        dateDisplay: TextView,
        batteryDisplay: TextView,
        weatherDisplay: TextView
    ) {
        setupTimeDisplay(timeDisplay)
        setupDateDisplay(dateDisplay)
        setupBatteryDisplay(batteryDisplay)
        setupWeatherDisplay(weatherDisplay)
        isActive = true
    }
    
    private fun setupTimeDisplay(timeDisplay: TextView) {
        timeUpdateHandler = Handler(Looper.getMainLooper())
        
        timeDisplay.setOnClickListener {
            listener?.onTimeClicked()
        }
        
        updateTime(timeDisplay)
        startTimeUpdates(timeDisplay)
    }
    
    private fun setupDateDisplay(dateDisplay: TextView) {
        updateDateDisplay(dateDisplay)
        
        dateDisplay.setOnClickListener {
            listener?.onDateClicked()
        }
    }
    
    private fun setupBatteryDisplay(batteryDisplay: TextView) {
        setupBatteryMonitoring(batteryDisplay)
        updateBattery(batteryDisplay)
    }
    
    private fun setupWeatherDisplay(weatherDisplay: TextView) {
        weatherManager = WeatherManager(context)
        weatherManager?.initialize(weatherDisplay)
    }
    
    private fun updateTime(timeDisplay: TextView) {
        if (!isActive) return
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        timeDisplay.text = currentTime
    }
    
    private fun startTimeUpdates(timeDisplay: TextView) {
        val updateTimeRunnable = object : Runnable {
            override fun run() {
                updateTime(timeDisplay)
                timeUpdateHandler?.postDelayed(this, 60000) // Update every minute
            }
        }
        timeUpdateHandler?.postDelayed(updateTimeRunnable, 60000)
    }
    
    private fun updateDateDisplay(dateDisplay: TextView) {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        dateDisplay.text = currentDate
    }
    
    private fun setupBatteryMonitoring(batteryDisplay: TextView) {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateBattery(batteryDisplay)
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }
    
    private fun updateBattery(batteryDisplay: TextView) {
        if (!isActive) return
        
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level != -1 && scale != -1) {
            val batteryPct = (level * 100 / scale)
            batteryDisplay.text = "🔋$batteryPct%"
        } else {
            batteryDisplay.text = "🔋--"
        }
    }
    
    fun openClockApp() {
        val clockPackages = listOf(
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.samsung.android.app.clockpackage",
            "com.htc.android.worldclock",
            "com.sec.android.app.clockpackage"
        )
        
        for (packageName in clockPackages) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                return
            }
        }
        
        val pm = context.packageManager
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
            context.startActivity(launchIntent)
        } else {
            try {
                val settingsIntent = Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
                context.startActivity(settingsIntent)
            } catch (e: Exception) {
                // If all else fails, do nothing
            }
        }
    }
    
    fun openCalendarApp() {
        val muditaIntent = context.packageManager.getLaunchIntentForPackage("com.mudita.calendar")
        if (muditaIntent != null) {
            context.startActivity(muditaIntent)
            return
        }
        
        val calendarIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
        }
        
        if (calendarIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(calendarIntent)
        } else {
            val dateIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("content://com.android.calendar/time")
            }
            if (dateIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(dateIntent)
            }
        }
    }
    
    fun destroy() {
        isActive = false
        
        timeUpdateHandler?.removeCallbacksAndMessages(null)
        timeUpdateHandler = null
        
        batteryReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
        batteryReceiver = null
        
        weatherManager?.destroy()
        weatherManager = null
    }
}