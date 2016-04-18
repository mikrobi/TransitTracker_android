package de.jakobclass.transittracker.factories

import android.graphics.Bitmap
import android.util.LruCache
import de.jakobclass.transittracker.models.Stop

object BitmapFactory {
    private val bitmapCache: LruCache<String, Bitmap>

    init {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        val cacheSize = maxMemory / 8
        bitmapCache = object: LruCache<String, Bitmap>(cacheSize.toInt()) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap?.byteCount ?: 0 / 1024
            }
        }
    }

    fun bitmapForStop(stop: Stop, scale: Float): Bitmap {
        val key = stop.vehicleTypes.map { it -> it.abbreviation }.joinToString("")
        var bitmap = bitmapCache.get(key)
        bitmap?.let { return it }

        bitmap = Bitmap(stop, scale)
        bitmapCache.put(key, bitmap)
        return bitmap
    }
}