package com.thaiduong.cybershield

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeMap
import com.thaiduong.cybershield.presentation.service.RefactoredControlService // Add this import

class ControlModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "ControlModule"

    @ReactMethod
    fun startControlService() {
        val intent = Intent(reactApplicationContext, RefactoredControlService::class.java)
        intent.action = RefactoredControlService.ACTION_START_SERVICE
        reactApplicationContext.startService(intent)
    }

    @ReactMethod
    fun stopControlService() {
        val intent = Intent(reactApplicationContext, RefactoredControlService::class.java)
        intent.action = RefactoredControlService.ACTION_STOP_SERVICE
        reactApplicationContext.startService(intent)
    }

    @ReactMethod
    fun isServiceRunning(promise: Promise) {
        val manager = reactApplicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RefactoredControlService::class.java.name == service.service.className) {
                promise.resolve(true)
                return
            }
        }
        promise.resolve(false)
    }

    @ReactMethod
    fun openSpecificAppSettings(settingType: String, promise: Promise) {
        val context = reactApplicationContext
        val packageName = context.packageName
        val intents = mutableListOf<Intent>()
        var success = false

        if (settingType == "usage") {
            // Simplified logic for Usage Access: Try standard, if fail, reject immediately.
            try {
                val usageIntent = Intent("android.settings.ACTION_USAGE_ACCESS_SETTINGS")
                usageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(usageIntent)
                success = true
            } catch (e: Exception) {
                android.util.Log.w("ControlModule", "Failed to start standard usage access settings", e)
                success = false
            }
        } else {
            // Keep the multi-path logic for other settings like notifications
            when (settingType) {
                "notification" -> {
                    intents.add(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    intents.add(Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                        putExtra("android.provider.extra.APP_PACKAGE", packageName)
                    })
                }
                // Can add other setting types here in the future
            }
            // Generic fallbacks for non-usage settings
            intents.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            })
            intents.add(Intent(Settings.ACTION_SETTINGS))

            for (intent in intents) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    success = true
                    break
                } catch (e: Exception) {
                    android.util.Log.w("ControlModule", "Failed to start activity for intent: $intent", e)
                }
            }
        }

        if (success) {
            promise.resolve("Successfully opened settings.")
        } else {
            if (settingType == "usage") {
                promise.reject("E_USAGE_ACCESS_NOT_FOUND", "Could not open usage access settings.")
            } else {
                promise.reject("E_SETTINGS_ERROR", "Could not open any relevant settings screen.")
            }
        }
    }

    @ReactMethod
    fun checkPermissions(promise: Promise) {
        val context = reactApplicationContext
        val packageName = context.packageName
        val notificationAccess = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.contains(packageName) == true
        
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        val usageAccess = mode == AppOpsManager.MODE_ALLOWED

        val permissions = WritableNativeMap().apply {
            putBoolean("notificationAccess", notificationAccess)
            putBoolean("usageAccess", usageAccess)
        }
        promise.resolve(permissions)
    }
}