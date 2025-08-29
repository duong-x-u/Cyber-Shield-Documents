package com.thaiduong.cybershield

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class HeadlessNotificationService : HeadlessJsTaskService() {
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        val extras = intent?.extras
        return if (extras != null) {
            HeadlessJsTaskConfig(
                "HandleNotification", // Tên task đã đăng ký ở JS
                Arguments.fromBundle(extras),
                10000L, // Timeout in ms
                true // Cho phép thử lại nếu thất bại
            )
        } else {
            null
        }
    }
}
