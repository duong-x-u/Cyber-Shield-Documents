package com.thaiduong.cybershield

import android.content.Intent
import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ReactActivity() {

  private val REQUEST_CODE_POST_NOTIFICATIONS = 1001

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

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
          }
      }
  }
}
