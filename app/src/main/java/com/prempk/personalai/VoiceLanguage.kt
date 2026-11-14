package com.prempk.personalai

/**
 * Lightweight script-based response-language detection.
 * Devanagari input selects Hindi; Latin-script input defaults to English (India).
 */
object VoiceLanguage {
    private val devanagari = Regex("[\\u0900-\\u097F]")

    fun detect(text: String): String =
        if (devanagari.containsMatchIn(text)) "hi-IN" else "en-IN"
}
