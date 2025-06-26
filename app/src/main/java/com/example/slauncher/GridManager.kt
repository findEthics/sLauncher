package com.example.slauncher

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridManager(
    private val context: Context,
    private val appSelectionManager: AppSelectionManager
) {
    
    private var appGridAdapter: AppGridAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var appCount: Int = 6
    
    interface GridUpdateListener {
        fun onAppSelected(position: Int, appInfo: AppInfo)
        fun onAppLaunched(appInfo: AppInfo)
    }
    
    private var listener: GridUpdateListener? = null
    
    fun setGridUpdateListener(listener: GridUpdateListener) {
        this.listener = listener
    }
    
    fun initializeGrid(
        recyclerView: RecyclerView,
        appCount: Int,
        selectedApps: List<AppInfo?>
    ) {
        this.appCount = appCount
        this.recyclerView = recyclerView
        
        setupRecyclerView(recyclerView, appCount)
        appGridAdapter?.updateSelectedApps(selectedApps)
        appGridAdapter?.preloadVisibleIcons()
    }
    
    private fun setupRecyclerView(recyclerView: RecyclerView, appCount: Int) {
        val columns = when (appCount) {
            2 -> 1
            4 -> 2 
            6 -> 2
            8 -> 2
            else -> 2
        }
        
        val layoutManager = GridLayoutManager(context, columns)
        recyclerView.layoutManager = layoutManager
        
        if (appGridAdapter == null) {
            appGridAdapter = AppGridAdapter(
                context = context,
                appCount = appCount,
                onAppClick = { position -> handleAppClick(position) },
                onAppLongClick = { position -> handleAppLongClick(position) }
            )
            recyclerView.adapter = appGridAdapter
        } else {
            appGridAdapter?.updateAppCount(appCount)
        }
    }
    
    private fun handleAppClick(position: Int) {
        val selectedApp = appGridAdapter?.getSelectedApp(position)
        if (selectedApp == null) {
            showAppSelectionDialog(position)
        } else {
            listener?.onAppLaunched(selectedApp)
        }
    }
    
    private fun handleAppLongClick(position: Int) {
        showAppSelectionDialog(position)
    }
    
    
    private fun showAppSelectionDialog(position: Int) {
        val installedApps = appSelectionManager.getInstalledApps()
        val appNamesList = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(context)
            .setTitle("Select App")
            .setItems(appNamesList) { _, which ->
                val selectedApp = installedApps[which]
                appGridAdapter?.updateAppAt(position, selectedApp)
                listener?.onAppSelected(position, selectedApp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    fun updateAppIcon(position: Int, appInfo: AppInfo) {
        appGridAdapter?.updateAppAt(position, appInfo)
    }
    
    fun getAppCount(): Int = appCount
    
    fun destroy() {
        appGridAdapter?.destroy()
        appGridAdapter = null
        recyclerView = null
    }
    
    fun refreshGrid(selectedApps: List<AppInfo?>) {
        appGridAdapter?.updateSelectedApps(selectedApps)
        appGridAdapter?.preloadVisibleIcons()
    }
}