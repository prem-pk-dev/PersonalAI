package com.prempk.personalai

/**
 * Bounds consecutive SpeechRecognizer failures so a permanent provider error
 * cannot create an endless microphone/restart loop.
 */
internal object SpeechRecognitionRetryPolicy {
    private val retryDelaysMs = longArrayOf(700L, 1_400L, 2_800L, 5_000L)

    /** Returns the delay for this 1-based consecutive failure, or null when exhausted. */
    fun delayAfterFailure(consecutiveFailures: Int): Long? {
        if (consecutiveFailures <= 0 || consecutiveFailures > retryDelaysMs.size) return null
        return retryDelaysMs[consecutiveFailures - 1]
    }

    fun maxRetries(): Int = retryDelaysMs.size
}
