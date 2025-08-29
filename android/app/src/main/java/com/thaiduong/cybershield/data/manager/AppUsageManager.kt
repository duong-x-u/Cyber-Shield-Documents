package com.thaiduong.cybershield.data.manager

import android.app.usage.UsageStatsManager
import android.content.Context

interface AppUsageManager {
    fun getForegroundApp(): String?
}

class AppUsageManagerImpl(
    private val context: Context
) : AppUsageManager {
    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val appList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )
        
        return appList?.let { stats ->
            if (stats.isNotEmpty()) {
                val sortedMap = stats.associateBy { it.lastTimeUsed }
                sortedMap[sortedMap.keys.maxOrNull()]?.packageName
            } else null
        }
    }
}
