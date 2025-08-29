package com.thaiduong.cybershield

import android.app.Application

import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.soloader.SoLoader
import com.thaiduong.cybershield.BuildConfig
import com.facebook.react.shell.MainReactPackage
import com.thaiduong.cybershield.ControlPackage

class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : ReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> {
                return listOf(
                    MainReactPackage(),
                    ControlPackage(),
                    io.invertase.notifee.NotifeePackage(),
                    com.reactnativecommunity.clipboard.ClipboardPackage(),
                    com.swmansion.gesturehandler.RNGestureHandlerPackage(),
                    com.oblador.keychain.KeychainPackage(),
                    com.zoontek.rnpermissions.RNPermissionsPackage(),
                    com.dieam.reactnativepushnotification.ReactNativePushNotificationPackage(),
                    com.th3rdwave.safeareacontext.SafeAreaContextPackage(),
                    com.swmansion.rnscreens.RNScreensPackage(),
                    com.oblador.vectoricons.VectorIconsPackage()
                    // com.janeasystems.rn_nodejs_mobile.RNNodeJsMobilePackage() // Temporarily disabled for debugging
                )
            }

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
        }

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, /* native exopackage */ false)
    }
}