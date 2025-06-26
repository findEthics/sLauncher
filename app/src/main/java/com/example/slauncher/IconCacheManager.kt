package com.example.slauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.*

class IconCacheManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: IconCacheManager? = null
        
        fun getInstance(context: Context): IconCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IconCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val CACHE_SIZE_MB = 4
        private const val ICON_SIZE_DP = 80
    }
    
    private val iconSize = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = minOf(maxMemory / 8, CACHE_SIZE_MB * 1024)
    
    private val iconCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
    
    private val loadingJobs = mutableMapOf<String, Deferred<Bitmap?>>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    fun getCachedIcon(packageName: String): Bitmap? {
        return iconCache.get(packageName)
    }
    
    suspend fun loadIconAsync(appInfo: AppInfo): Bitmap? {
        val cachedIcon = getCachedIcon(appInfo.packageName)
        if (cachedIcon != null) {
            return cachedIcon
        }
        
        // Check if already loading
        loadingJobs[appInfo.packageName]?.let { job ->
            return job.await()
        }
        
        // Start loading
        val job = scope.async(Dispatchers.IO) {
            try {
                val drawable = appInfo.icon
                val bitmap = drawableToBitmap(drawable, iconSize, iconSize)
                
                // Cache the bitmap
                withContext(Dispatchers.Main) {
                    iconCache.put(appInfo.packageName, bitmap)
                    loadingJobs.remove(appInfo.packageName)
                }
                
                bitmap
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingJobs.remove(appInfo.packageName)
                }
                null
            }
        }
        
        loadingJobs[appInfo.packageName] = job
        return job.await()
    }
    
    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val originalBitmap = drawable.bitmap
            if (originalBitmap.width == width && originalBitmap.height == height) {
                return originalBitmap
            }
            return Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    fun preloadIcons(apps: List<AppInfo>) {
        scope.launch(Dispatchers.IO) {
            apps.forEach { appInfo ->
                if (getCachedIcon(appInfo.packageName) == null) {
                    loadIconAsync(appInfo)
                }
            }
        }
    }
    
    fun clearCache() {
        iconCache.evictAll()
        loadingJobs.clear()
    }
    
    fun getCacheInfo(): String {
        return "Cache: ${iconCache.size()} icons, ${iconCache.size() * 100 / cacheSize}% full"
    }
    
    fun destroy() {
        scope.cancel()
        loadingJobs.clear()
        iconCache.evictAll()
    }
}