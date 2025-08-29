package com.thaiduong.cybershield.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.thaiduong.cybershield.MainActivity
import com.thaiduong.cybershield.R
import com.thaiduong.cybershield.data.manager.AppUsageManagerImpl
import com.thaiduong.cybershield.data.repository.GameRepositoryImpl
import com.thaiduong.cybershield.domain.usecase.CheckGamingModeUseCase
import com.thaiduong.cybershield.domain.usecase.MonitorGamingModeUseCase
import com.thaiduong.cybershield.di.ServiceContainer

class RefactoredControlService : Service(), MonitorGamingModeUseCase.GamingModeListener {

    private lateinit var monitorGamingModeUseCase: MonitorGamingModeUseCase
    private lateinit var appUsageManager: AppUsageManagerImpl

    companion object {
        var isGamingModeActive = false
            private set
        
        const val CHANNEL_ID = "ControlServiceChannel"
        const val GAMING_MODE_CHANNEL_ID = "GamingModeChannel"
        const val NOTIFICATION_ID = 111
        const val GAMING_MODE_NOTIFICATION_ID = 112
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        initializeDependencies()
    }

    private fun initializeDependencies() {
        val gameRepository = ServiceContainer.provideGameRepository()
        val appUsageManager = ServiceContainer.provideAppUsageManager(this)
        val checkGamingModeUseCase = ServiceContainer.provideCheckGamingModeUseCase(gameRepository)
        
        monitorGamingModeUseCase = ServiceContainer.provideMonitorGamingModeUseCase(
            checkGamingModeUseCase,
            appUsageManager
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startService()
            ACTION_STOP_SERVICE -> stopService()
        }
        return START_STICKY
    }

    private fun startService() {
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        monitorGamingModeUseCase.startMonitoring(this)
        Log.d("RefactoredControlService", "Service started successfully")
    }

    private fun stopService() {
        monitorGamingModeUseCase.stopMonitoring()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d("RefactoredControlService", "Service stopped")
    }

    override fun onGamingModeEntered() {
        isGamingModeActive = true
        showGamingModeNotification()
        Log.d("RefactoredControlService", "Gaming mode activated")
    }

    override fun onGamingModeExited() {
        isGamingModeActive = false
        cancelGamingModeNotification()
        Log.d("RefactoredControlService", "Gaming mode deactivated")
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingOpenAppIntent = PendingIntent.getActivity(
            this, 
            1, 
            openAppIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RefactoredControlService::class.java).apply { 
            action = ACTION_STOP_SERVICE 
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 
            0, 
            stopIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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

    private fun cancelGamingModeNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(GAMING_MODE_NOTIFICATION_ID)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Lối tắt khẩn cấp CyberShield", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Thông báo thường trực để truy cập nhanh tính năng phân tích."
        }
        manager.createNotificationChannel(serviceChannel)

        val gamingChannel = NotificationChannel(GAMING_MODE_CHANNEL_ID, "Cảnh báo Chế độ chơi game", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Thông báo khi tính năng quét bị tạm dừng do chơi game."
        }
        manager.createNotificationChannel(gamingChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitorGamingModeUseCase.stopMonitoring()
    }
}
