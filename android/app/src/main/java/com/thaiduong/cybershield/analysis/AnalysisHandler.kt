package com.thaiduong.cybershield.analysis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.thaiduong.cybershield.MainActivity
import com.thaiduong.cybershield.R
import com.thaiduong.cybershield.presentation.service.RefactoredControlService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Data classes for API communication
data class AnalysisRequest(val text: String, val urls: List<String>)
data class AnalysisResponse(
    val is_scam: Boolean,
    val score: Int,
    val reason: String?,
    val original_text: String?
)

data class FullApiResponse(
    val result: AnalysisResponse
)

object AnalysisHandler {
    private const val TAG = "AnalysisHandler"
    private const val ANALYSIS_API_URL = "https://cybershield-backend-renderserver.onrender.com/api/analyze"
    private const val ANALYSIS_CHANNEL_ID = "analysis_results_channel"

    private val okHttpClient = OkHttpClient()
    private val gson = Gson()
    private var lastAnalyzedText: String? = null
    private var lastAnalysisTime: Long = 0
    private const val DEBOUNCE_INTERVAL = 3000 // 3 seconds

    private fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = android.util.Patterns.WEB_URL.matcher(text)
        while (matcher.find()) {
            urls.add(matcher.group())
        }
        return urls
    }

    fun analyzeText(context: Context, text: String) {
        // Debounce: if the same text was analyzed recently, skip.
        val currentTime = System.currentTimeMillis()
        if (text == lastAnalyzedText && (currentTime - lastAnalysisTime) < DEBOUNCE_INTERVAL) {
            Log.d(TAG, "Skipping analysis for recently analyzed text.")
            return
        }

        // Skip analysis if gaming mode is active
        if (RefactoredControlService.isGamingModeActive) {
            Log.d(TAG, "Gaming mode is active, skipping analysis.")
            return
        }

        lastAnalyzedText = text
        lastAnalysisTime = currentTime

        Log.d(TAG, "Starting analysis for text: $text")

        val urls = extractUrls(text)
        val requestData = AnalysisRequest(text, urls)
        val requestBody = gson.toJson(requestData).toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(ANALYSIS_API_URL)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "API call failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call unsuccessful: ${response.code}")
                    return
                }

                response.body?.string()?.let { responseBody ->
                    Log.d(TAG, "Raw API Response: $responseBody")
                    try {
                        val fullResponse = gson.fromJson(responseBody, FullApiResponse::class.java)
                        val result = fullResponse.result
                        Log.d(TAG, "Parsed is_scam: ${result.is_scam}")
                        showAnalysisNotification(context, result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse API response", e)
                    }
                }
            }
        })
    }

    private fun showAnalysisNotification(context: Context, result: AnalysisResponse) {
        val title: String
        val body: String

        if (result.is_scam) {
            title = "CyberShield: Nguy hiểm!"
            val reason = result.reason ?: "Không có lý do cụ thể."
            body = "Kết quả: Đoạn văn bản trông có vẻ nguy hiểm nha bro\nLý do: $reason"
        } else {
            title = "CyberShield: An toàn"
            body = "Kết quả: An toàn"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, ANALYSIS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(ANALYSIS_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                ANALYSIS_CHANNEL_ID,
                "Kết quả phân tích CyberShield",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo kết quả phân tích tin nhắn lừa đảo."
            }
            manager.createNotificationChannel(channel)
        }
    }
}
