package com.thaiduong.cybershield

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.thaiduong.cybershield.R

class ControlService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStatsManager: UsageStatsManager
    private var isRunning = false
    private var previousApp: String? = null

    companion object {
        var isGamingModeActive = false
        const val CHANNEL_ID = "ControlServiceChannel"
        const val GAMING_MODE_CHANNEL_ID = "GamingModeChannel"
        const val NOTIFICATION_ID = 111
        const val GAMING_MODE_NOTIFICATION_ID = 112
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

        val HEAVY_GAMES = setOf(
            "com.garena.game.kgvn",       // Liên Quân Mobile
            "com.dts.freefireth",        // Garena Free Fire
            "com.vng.pubgmobile",        // PUBG Mobile
            "com.miHoYo.GenshinImpact"   // Genshin Impact
            // Bạn có thể thêm các package name khác vào đây
        )
    }

    private val checkAppRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val currentApp = getForegroundApp()
            val isCurrentlyInGame = currentApp in HEAVY_GAMES
            val wasPreviouslyInGame = previousApp in HEAVY_GAMES

            if (isCurrentlyInGame && !wasPreviouslyInGame) {
                // User has just entered a game
                Log.d("ControlService", "Entering gaming mode. Pausing scans.")
                isGamingModeActive = true
                showGamingModeNotification()
            } else if (!isCurrentlyInGame && wasPreviouslyInGame) {
                // User has just exited a game
                Log.d("ControlService", "Exiting gaming mode. Resuming scans.")
                isGamingModeActive = false
                // Optionally, cancel the gaming mode notification if it's persistent
                 val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                 manager.cancel(GAMING_MODE_NOTIFICATION_ID)
            }

            previousApp = currentApp
            handler.postDelayed(this, 1000) // Lặp lại sau 1 giây
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startServiceLogic()
            ACTION_STOP_SERVICE -> stopServiceLogic()
        }
        return START_STICKY
    }
    
    private fun startServiceLogic() {
        if (isRunning) return
        isRunning = true
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        handler.post(checkAppRunnable)
        Log.d("ControlService", "Service started.")
    }

    private fun stopServiceLogic() {
        isRunning = false
        isGamingModeActive = false
        handler.removeCallbacks(checkAppRunnable)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d("ControlService", "Service stopped.")
    }

    private fun getForegroundApp(): String? {
        var currentApp: String? = null
        val time = System.currentTimeMillis()
        val appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        if (appList != null && appList.isNotEmpty()) {
            val sortedMap = appList.associateBy { it.lastTimeUsed }
            if (sortedMap.isNotEmpty()) {
                currentApp = sortedMap[sortedMap.keys.maxOrNull()]?.packageName
            }
        }
        return currentApp
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingOpenAppIntent = PendingIntent.getActivity(this, 1, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, ControlService::class.java).apply { action = ACTION_STOP_SERVICE }
        val pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CyberShield đang bảo vệ")
            .setContentText("Nhấn để kiểm tra tin nhắn đáng ngờ từ clipboard.")
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setContentIntent(pendingOpenAppIntent)
            .addAction(R.drawable.ic_stop_service, "Tắt Bảo Vệ", pendingStopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showGamingModeNotification() {
        val notification = NotificationCompat.Builder(this, GAMING_MODE_CHANNEL_ID)
            .setContentTitle("CyberShield: Chế độ chơi game")
            .setContentText("Đã tạm ngưng quét tự động để đảm bảo hiệu năng chơi game.")
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(GAMING_MODE_NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        
        // Channel for the main persistent service notification
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Lối tắt khẩn cấp CyberShield", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Thông báo thường trực để truy cập nhanh tính năng phân tích."
        }
        manager.createNotificationChannel(serviceChannel)

        // Channel for gaming mode alerts
        val gamingChannel = NotificationChannel(GAMING_MODE_CHANNEL_ID, "Cảnh báo Chế độ chơi game", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Thông báo khi tính năng quét bị tạm dừng do chơi game."
        }
        manager.createNotificationChannel(gamingChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopServiceLogic()
    }
}

