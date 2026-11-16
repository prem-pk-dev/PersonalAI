package com.prempk.personalai

import java.util.Locale

/**
 * Resolves only narrow, explicit references to the last successfully launched app.
 * No free-form text or generated instructions are ever executed from conversation history.
 */
object FollowUpCommandResolver {
    private val reopenPhrases = setOf(
        "open it again",
        "reopen it",
        "open that again",
        "launch it again",
        "open the same app",
        "reopen the same app",
        "उसे फिर से खोलो",
        "उसे दोबारा खोलो",
        "उस ऐप को फिर से खोलो",
        "वही ऐप खोलो",
        "वही एप खोलो",
        "उसी ऐप को खोलो",
        "same app kholo",
        "wahi app kholo",
        "use phir se kholo",
        "usi app ko kholo"
    )

    fun resolve(raw: String, lastOpenedAppName: String?): String? {
        val appName = lastOpenedAppName?.trim()?.takeIf { it.isNotEmpty() && it.length <= 80 }
            ?: return null
        val normalized = raw.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[.!?।]+$"), "")
            .replace(Regex("^hey\\s+pk[, ]*"), "")
            .trim()
        return if (normalized in reopenPhrases) "open $appName" else null
    }
}
