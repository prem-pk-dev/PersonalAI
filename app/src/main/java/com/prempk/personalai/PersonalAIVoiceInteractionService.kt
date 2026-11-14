package com.prempk.personalai

import android.service.voice.VoiceInteractionService

/**
 * Lightweight entry point registered with Android when PersonalAI is selected
 * as the user's voice interaction service.
 *
 * Hotword detection is intentionally not started here until a supported,
 * user-consented detection engine and device capability are integrated.
 */
class PersonalAIVoiceInteractionService : VoiceInteractionService()
