package com.thaiduong.cybershield

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.thaiduong.cybershield.analysis.AnalysisHandler

class ProcessTextActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the selected text from the intent that started this activity.
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)

        // If there is text, pass it to the background analysis handler.
        if (text != null && text.isNotBlank()) {
            AnalysisHandler.analyzeText(applicationContext, text.toString())
        }

        // This activity is translucent and should be closed immediately after
        // firing the background task. The user never sees it.
        finish()
    }
}
