package com.prempk.personalai

import android.service.voice.VoiceInteractionSession
import android.view.View
import android.widget.TextView

/**
 * Minimal system-invoked assistant surface.
 * Command capture and execution remain in the app's existing, user-visible flow.
 */
class PersonalAIVoiceInteractionSession(context: android.content.Context) :
    VoiceInteractionSession(context) {

    override fun onCreateContentView(): View {
        return TextView(context).apply {
            text = "PersonalAI\n\nAssistant session ready. Open PersonalAI to start PK listening."
            textSize = 18f
            setPadding(40, 48, 40, 48)
            setTextColor(0xFFF3F7FB.toInt())
            setBackgroundColor(0xFF08111F.toInt())
        }
    }
}
