package com.prempk.personalai

/** Recognizes the user's short PK wake word in English or Hindi speech transcripts. */
object WakeWordParser {
    private val pattern = Regex("(?i)(?<![a-z0-9])p\\s*k(?![a-z0-9])|पी\\s*के")

    /**
     * Returns null when no wake word is present, an empty string for wake-only,
     * or the remaining command when the wake word and command are in one utterance.
     */
    fun extractCommand(transcript: String): String? {
        val match = pattern.find(transcript.lowercase()) ?: return null
        return transcript.removeRange(match.range).trim(' ', ',', '.', '!', '?')
    }
}
