package com.example.slauncher

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class AppGridAdapter(
    private val context: Context,
    private var appCount: Int,
    private val onAppClick: (position: Int) -> Unit,
    private val onAppLongClick: (position: Int) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.AppViewHolder>() {
    
    private var selectedApps: MutableList<AppInfo?> = MutableList(appCount) { null }
    private val iconCacheManager = IconCacheManager.getInstance(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_app_item, parent, false)
        return AppViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = selectedApps.getOrNull(position)
        
        // Set default icon first
        holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        
        if (appInfo != null) {
            loadAppIcon(holder, appInfo)
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener {
            onAppClick(position)
        }
        
        holder.itemView.setOnLongClickListener {
            onAppLongClick(position)
            true
        }
    }
    
    private fun loadAppIcon(holder: AppViewHolder, appInfo: AppInfo) {
        // Check cache first
        val cachedIcon = iconCacheManager.getCachedIcon(appInfo.packageName)
        if (cachedIcon != null) {
            holder.appIcon.setImageBitmap(cachedIcon)
            return
        }
        
        // Load asynchronously
        coroutineScope.launch {
            val bitmap = iconCacheManager.loadIconAsync(appInfo)
            if (bitmap != null) {
                holder.appIcon.setImageBitmap(bitmap)
            } else {
                // Fallback to original drawable if bitmap loading fails
                holder.appIcon.setImageDrawable(appInfo.icon)
            }
        }
    }
    
    override fun getItemCount(): Int = appCount
    
    fun updateSelectedApps(newSelectedApps: List<AppInfo?>) {
        selectedApps.clear()
        selectedApps.addAll(newSelectedApps)
        
        // Ensure we have the right number of items
        while (selectedApps.size < appCount) {
            selectedApps.add(null)
        }
        if (selectedApps.size > appCount) {
            selectedApps = selectedApps.take(appCount).toMutableList()
        }
        
        notifyDataSetChanged()
    }
    
    fun updateAppCount(newAppCount: Int) {
        val oldCount = appCount
        appCount = newAppCount
        
        if (newAppCount > oldCount) {
            // Add null entries for new positions
            repeat(newAppCount - oldCount) {
                selectedApps.add(null)
            }
            notifyItemRangeInserted(oldCount, newAppCount - oldCount)
        } else if (newAppCount < oldCount) {
            // Remove excess entries
            selectedApps = selectedApps.take(newAppCount).toMutableList()
            notifyItemRangeRemoved(newAppCount, oldCount - newAppCount)
        }
    }
    
    fun updateAppAt(position: Int, appInfo: AppInfo) {
        if (position < selectedApps.size) {
            selectedApps[position] = appInfo
            notifyItemChanged(position)
        }
    }
    
    fun getSelectedApp(position: Int): AppInfo? {
        return selectedApps.getOrNull(position)
    }
    
    fun preloadVisibleIcons() {
        val appsToPreload = selectedApps.filterNotNull()
        iconCacheManager.preloadIcons(appsToPreload)
    }
    
    fun destroy() {
        coroutineScope.cancel()
    }
}