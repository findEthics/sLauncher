package com.example.slauncher

import android.content.Context
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog

class GridManager(
    private val context: Context,
    private val appSelectionManager: AppSelectionManager
) {
    
    private var appIcons: MutableList<ImageView> = mutableListOf()
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
        container: GridLayout,
        appCount: Int,
        selectedApps: List<AppInfo?>
    ) {
        this.appCount = appCount
        appIcons.clear()
        
        container.removeAllViews()
        configureGridLayout(container)
        
        for (i in 0 until appCount) {
            val imageView = createAppIcon(i)
            appIcons.add(imageView)
            container.addView(imageView)
            
            selectedApps.getOrNull(i)?.let { appInfo ->
                imageView.setImageDrawable(appInfo.icon)
            }
        }
        
        setupClickListeners(selectedApps)
    }
    
    private fun configureGridLayout(container: GridLayout) {
        val (columns, rows) = when (appCount) {
            2 -> Pair(1, 2)
            4 -> Pair(2, 2)
            6 -> Pair(2, 3)
            8 -> Pair(2, 4)
            else -> Pair(2, 3)
        }
        
        container.columnCount = columns
        container.rowCount = rows
    }
    
    private fun createAppIcon(index: Int): ImageView {
        return ImageView(context).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = (80 * context.resources.displayMetrics.density).toInt()
                height = (80 * context.resources.displayMetrics.density).toInt()
                setMargins(
                    (16 * context.resources.displayMetrics.density).toInt(),
                    (16 * context.resources.displayMetrics.density).toInt(),
                    (16 * context.resources.displayMetrics.density).toInt(),
                    (16 * context.resources.displayMetrics.density).toInt()
                )
            }
            
            val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            background = typedArray.getDrawable(0)
            typedArray.recycle()
            
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(
                (8 * context.resources.displayMetrics.density).toInt(),
                (8 * context.resources.displayMetrics.density).toInt(),
                (8 * context.resources.displayMetrics.density).toInt(),
                (8 * context.resources.displayMetrics.density).toInt()
            )
            setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }
    
    private fun setupClickListeners(selectedApps: List<AppInfo?>) {
        appIcons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                val selectedApp = selectedApps.getOrNull(index)
                if (selectedApp == null) {
                    showAppSelectionDialog(index)
                } else {
                    listener?.onAppLaunched(selectedApp)
                }
            }
            
            imageView.setOnLongClickListener {
                showAppSelectionDialog(index)
                true
            }
        }
    }
    
    private fun showAppSelectionDialog(position: Int) {
        val installedApps = appSelectionManager.getInstalledApps()
        val appNamesList = installedApps.map { it.appName }.toTypedArray()
        
        AlertDialog.Builder(context)
            .setTitle("Select App")
            .setItems(appNamesList) { _, which ->
                val selectedApp = installedApps[which]
                appIcons[position].setImageDrawable(selectedApp.icon)
                listener?.onAppSelected(position, selectedApp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    fun updateAppIcon(position: Int, appInfo: AppInfo) {
        if (position < appIcons.size) {
            appIcons[position].setImageDrawable(appInfo.icon)
        }
    }
    
    fun getAppCount(): Int = appCount
}