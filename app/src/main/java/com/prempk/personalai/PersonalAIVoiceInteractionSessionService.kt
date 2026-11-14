package com.prempk.personalai

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

/** Creates the short-lived UI session requested by Android's assistant framework. */
class PersonalAIVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle): VoiceInteractionSession {
        return PersonalAIVoiceInteractionSession(this)
    }
}
