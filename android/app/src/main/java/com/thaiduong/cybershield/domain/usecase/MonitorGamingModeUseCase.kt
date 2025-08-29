package com.thaiduong.cybershield.domain.usecase

import android.os.Handler
import android.os.Looper
import com.thaiduong.cybershield.data.manager.AppUsageManager

class MonitorGamingModeUseCase(
    private val checkGamingModeUseCase: CheckGamingModeUseCase,
    private val appUsageManager: AppUsageManager
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var previousApp: String? = null
    private var listener: GamingModeListener? = null

    interface GamingModeListener {
        fun onGamingModeEntered()
        fun onGamingModeExited()
    }

    fun startMonitoring(listener: GamingModeListener) {
        if (isRunning) return
        this.listener = listener
        isRunning = true
        handler.post(checkAppRunnable)
    }

    fun stopMonitoring() {
        isRunning = false
        handler.removeCallbacks(checkAppRunnable)
        listener = null
    }

    private val checkAppRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val currentApp = appUsageManager.getForegroundApp()
            val isCurrentlyInGame = checkGamingModeUseCase.execute(currentApp)
            val wasPreviouslyInGame = checkGamingModeUseCase.execute(previousApp)

            when {
                isCurrentlyInGame && !wasPreviouslyInGame -> {
                    listener?.onGamingModeEntered()
                }
                !isCurrentlyInGame && wasPreviouslyInGame -> {
                    listener?.onGamingModeExited()
                }
            }

            previousApp = currentApp
            handler.postDelayed(this, 1000)
        }
    }
}
