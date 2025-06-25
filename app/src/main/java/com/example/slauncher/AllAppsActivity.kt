package com.example.slauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AllAppsActivity : AppCompatActivity() {
    
    private lateinit var installedApps: List<AppInfo>
    private lateinit var appsListView: ListView
    private lateinit var searchEditText: EditText
    private lateinit var settingsButton: ImageButton
    private lateinit var adapter: AllAppsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)
        
        appsListView = findViewById(R.id.apps_list_view)
        searchEditText = findViewById(R.id.search_edit_text)
        settingsButton = findViewById(R.id.settings_button)
        
        loadInstalledApps()
        setupAppsList()
        setupSearch()
        setupSettingsButton()
        showKeyboard()
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
    
    private fun setupAppsList() {
        adapter = AllAppsAdapter(this, installedApps)
        appsListView.adapter = adapter
        
        appsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = adapter.getItem(position) as AppInfo
            launchApp(selectedApp)
        }
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun showKeyboard() {
        searchEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun setupSettingsButton() {
        settingsButton.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun showSettingsDialog() {
        val sharedPreferences = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val currentAppCount = sharedPreferences.getInt("app_count", 6)
        
        val appCountOptions = arrayOf("3", "4", "5", "6", "7", "8", "9", "10")
        val currentSelection = appCountOptions.indexOf(currentAppCount.toString())
        
        AlertDialog.Builder(this)
            .setTitle("Home Screen Apps")
            .setSingleChoiceItems(appCountOptions, currentSelection) { dialog, which ->
                val selectedCount = appCountOptions[which].toInt()
                sharedPreferences.edit()
                    .putInt("app_count", selectedCount)
                    .apply()
                dialog.dismiss()
                
                // Show confirmation message
                AlertDialog.Builder(this)
                    .setTitle("Settings Updated")
                    .setMessage("Home screen will now display $selectedCount apps. Go back to see the changes.")
                    .setPositiveButton("OK", null)
                    .show()
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
}