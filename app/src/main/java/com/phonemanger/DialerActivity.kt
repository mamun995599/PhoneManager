package com.phonemanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.phonemanager.manager.CallManager
import com.phonemanager.manager.CallStateManager

/**
 * This activity is required for the app to be set as the default dialer.
 * It handles DIAL intents and passes them to the system dialer or processes them.
 */
class DialerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DialerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "DialerActivity started with intent: ${intent?.action}")

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${intent?.action}")
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_DIAL, Intent.ACTION_VIEW, Intent.ACTION_CALL_BUTTON -> {
                val number = intent.data?.schemeSpecificPart
                Log.d(TAG, "Dial request for number: $number")

                if (number != null) {
                    // Store the number for tracking
                    CallStateManager.currentCallNumber = number

                    // Open system dialer with the number
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$number")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(dialIntent)
                } else {
                    // Just open main activity
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            else -> {
                // Open main activity for other intents
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        finish()
    }
}