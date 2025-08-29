package com.thaiduong.cybershield

import android.content.Intent
import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule

class MainActivity : ReactActivity() {

  override fun getMainComponentName(): String = "CyberShield"

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
  }

  private fun handleSendIntent(intent: Intent?) {
    if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
            // Send event to React Native
            val reactContext = reactInstanceManager.currentReactContext
            if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
                val params = Arguments.createMap().apply {
                    putString("text", text)
                }
                reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit("onProcessText", params)
            }
        }
    }
  }

  override fun onNewIntent(intent: Intent?) {
      super.onNewIntent(intent)
      handleSendIntent(intent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(null)
      handleSendIntent(intent)
  }
}
