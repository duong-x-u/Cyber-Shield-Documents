package com.thaiduong.cybershield

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.thaiduong.cybershield.analysis.AnalysisHandler
import com.thaiduong.cybershield.presentation.service.RefactoredControlService

class NotificationListener : NotificationListenerService() {

    // Throttle checks per package to avoid spamming the analysis server
    private val lastCheckMap = mutableMapOf<String, Long>()
    private val THROTTLE_INTERVAL = 2000 // 2 seconds per app

    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            Log.d(TAG, "onNotificationPosted: sbn is null, returning.")
            return
        }

        val notificationCategory = sbn.notification.category
        if (notificationCategory == Notification.CATEGORY_SYSTEM || notificationCategory == Notification.CATEGORY_SERVICE) {
            Log.d(TAG, "Skipping system/service notification from ${sbn.packageName}, category: $notificationCategory")
            return
        }

        // In gaming mode, all automatic scans are paused.
        if (RefactoredControlService.isGamingModeActive) {
            Log.d(TAG, "Gaming mode is active, skipping notification analysis.")
            return
        }

        val packageName = sbn.packageName
        val currentTime = System.currentTimeMillis()

        // Throttle notifications from the same app to avoid spam.
        val lastCheck = lastCheckMap[packageName] ?: 0
        if (currentTime - lastCheck < THROTTLE_INTERVAL) {
            Log.d(TAG, "Notification from $packageName throttled, skipping.")
            return
        }
        lastCheckMap[packageName] = currentTime

        // Define target apps for analysis.
        val isTargetApp = packageName in listOf(
            "com.zing.zalo",
            "com.facebook.orca",
            "org.telegram.messenger"
        ) || packageName.contains("sms") || packageName.contains("message")

        // Ignore notifications from self or non-target apps.
        if (!isTargetApp || packageName == applicationContext.packageName) {
            Log.d(TAG, "Notification from non-target app or self: $packageName, returning.")
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""

        // Try to extract text from various notification fields.
        var text: String? = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        if (text.isNullOrEmpty()) {
            text = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
        }
        if (text.isNullOrEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
             val remoteInputs = Notification.MessagingStyle.Message.getMessagesFromBundleArray(
                extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            )
            if (!remoteInputs.isNullOrEmpty()) {
                text = remoteInputs.last().text.toString()
            }
        }
        if (text.isNullOrEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        }


        if (text.isNullOrEmpty() || text.isBlank()) {
            Log.d(TAG, "Notification from $packageName has no usable text, skipping.")
            return
        }

        Log.d(TAG, "Captured text from [$packageName] ($title) -> Forwarding to AnalysisHandler")

        // Instead of sending an event to JS, directly call the centralized AnalysisHandler.
        AnalysisHandler.analyzeText(applicationContext, text)
    }
}