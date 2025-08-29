package com.thaiduong.cybershield

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.ReactApplication

class ProcessTextActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy đoạn văn bản được chọn từ Intent
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)

        // Gửi dữ liệu về React Native
        if (text != null) {
            val reactContext = (application as ReactApplication).reactNativeHost.reactInstanceManager.currentReactContext
            if (reactContext != null) {
                sendEventToReactNative(reactContext, "onProcessText", text.toString())
            }
        }

        // Đóng Activity này ngay lập tức
        finish()
    }

    private fun sendEventToReactNative(reactContext: ReactContext, eventName: String, data: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, Arguments.createMap().apply { putString("text", data) })
    }
}