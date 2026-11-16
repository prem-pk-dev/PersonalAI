package com.prempk.personalai

import java.util.Locale

/**
 * Resolves only narrow, explicit references to the last successfully launched app.
 * The exact package name is returned so similarly named apps cannot be confused.
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
    private val packageNamePattern = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")

    fun resolvePackage(raw: String, lastOpenedPackageName: String?): String? {
        val packageName = lastOpenedPackageName?.trim()
            ?.takeIf { it.length <= 255 && packageNamePattern.matches(it) }
            ?: return null
        val normalized = raw.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[.!?।]+$"), "")
            .replace(Regex("^hey\\s+pk[, ]*"), "")
            .trim()
        return packageName.takeIf { normalized in reopenPhrases }
    }
}
