package com.thaiduong.cybershield

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule

class NotificationListener : NotificationListenerService() {

    // Lưu last check theo package để không throttle hết
    private val lastCheckMap = mutableMapOf<String, Long>()
    private val THROTTLE_INTERVAL = 2000 // 2 giây/app

    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Gaming mode → bỏ qua
        if (ControlService.isGamingModeActive) {
            Log.d(TAG, "Gaming mode is active, skipping notification.")
            return
        }

        val packageName = sbn.packageName
        val currentTime = System.currentTimeMillis()

        // Chặn spam cùng app
        val lastCheck = lastCheckMap[packageName] ?: 0
        if (currentTime - lastCheck < THROTTLE_INTERVAL) {
            Log.d(TAG, "Notification from $packageName throttled, skipping.")
            return
        }
        lastCheckMap[packageName] = currentTime

        val isTargetApp = packageName in listOf(
            "com.zing.zalo",
            "com.facebook.orca",
            "org.telegram.messenger"
        ) || packageName.contains("sms") || packageName.contains("message")

        if (!isTargetApp || packageName == applicationContext.packageName) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""

        // Lấy text từ nhiều nguồn để chắc ăn
        var text: String? = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        if (text.isNullOrEmpty()) {
            text = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
        }
        if (text.isNullOrEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        }

        if (text.isNullOrEmpty()) {
            Log.d(TAG, "Notification from $packageName has no text → skipped")
            return
        }

        Log.d(TAG, "Captured [$packageName] $title: $text")

        // Đẩy về React Native
        val reactContext = (application as? ReactApplication)
            ?.reactNativeHost
            ?.reactInstanceManager
            ?.currentReactContext

        if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
            sendEventToReactNative(reactContext, "onNotificationReceived", text)
        } else {
            Log.w(TAG, "ReactContext null or inactive → cannot send event")
        }
    }

    private fun sendEventToReactNative(reactContext: ReactContext, eventName: String, data: String) {
        val params = Arguments.createMap().apply { putString("text", data) }
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
