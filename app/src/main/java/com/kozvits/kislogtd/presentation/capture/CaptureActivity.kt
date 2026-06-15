package com.kozvits.kislogtd.presentation.capture

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.kozvits.kislogtd.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                // Pass text back to main app via intent
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("capture_text", text)
                }
                startActivity(mainIntent)
                finish()
            }
            else -> finish()
        }
    }
}
