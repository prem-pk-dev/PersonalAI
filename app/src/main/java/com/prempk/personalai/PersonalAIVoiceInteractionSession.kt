package com.prempk.personalai

import android.content.Intent
import android.service.voice.VoiceInteractionSession
import android.view.View
import android.widget.TextView

/**
 * Minimal Android-invoked assistant surface.
 * Tapping it opens the full app; PK recognition remains explicitly user-started.
 */
class PersonalAIVoiceInteractionSession(context: android.content.Context) :
    VoiceInteractionSession(context) {

    override fun onCreateContentView(): View {
        return TextView(context).apply {
            text = "PersonalAI\\n\\nTap to open PersonalAI and start PK listening."
            textSize = 18f
            setPadding(40, 48, 40, 48)
            setTextColor(0xFFF3F7FB.toInt())
            setBackgroundColor(0xFF08111F.toInt())
            isClickable = true
            setOnClickListener {
                val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (launchIntent != null) context.startActivity(launchIntent)
                hide()
            }
        }
    }
}
