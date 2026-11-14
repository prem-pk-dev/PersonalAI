package com.prempk.personalai

/**
 * Lightweight language hinting for assistant replies.
 * Devanagari selects Hindi; common Romanized Hindi command words also select Hindi.
 * Other Latin-script input defaults to English (India).
 */
object VoiceLanguage {
    private val devanagari = Regex("[\\u0900-\\u097F]")
    private val romanHindi = Regex(
        "(?i)\\b(kya|hai|hain|kholo|khol|batao|bataye|kitna|kitni|kitne|karo|karna|bhejo|lagao|mera|meri|mujhe|aap|abhi|samay|tareekh|kaise|kyun|kyu|chahiye|dikhao|sunao|bolo|haan|nahi|nahin|ji)\\b"
    )

    fun detect(text: String): String =
        if (devanagari.containsMatchIn(text) || romanHindi.containsMatchIn(text)) "hi-IN" else "en-IN"
}
