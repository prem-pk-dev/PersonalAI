package com.prempk.personalai

import java.util.Locale

/**
 * Deterministic parsers for alarm and timer utterances.
 * Returns null for ambiguous or out-of-range values rather than guessing.
 */
object AlarmTimeParser {
    private val timeToken = Regex(
        "(\\\\d{1,2})(?::(\\\\d{2}))?\\\\s*(a\\\\.?m\\\\.?|p\\\\.?m\\\\.?|बजे)?",
        RegexOption.IGNORE_CASE
    )
    private val timePrefix = Regex(
        "(?:\\\\b(?:at|around)\\\\s*|\\\\b(?:alarm)\\\\s*(?:at\\\\s*)?|अलार्म\\\\s*(?:पर\\\\s*)?|(?:सुबह|सवेरे|भोर|दोपहर|शाम|रात)\\\\s*)$",
        RegexOption.IGNORE_CASE
    )
    private val durationSuffix = Regex(
        "^\\\\s*(?:minutes?|mins?|hours?|hrs?|seconds?|secs?|मिनट|घंटे?|सेकंड)",
        RegexOption.IGNORE_CASE
    )

    fun parseMinutes(command: String): Int? {
        val normalized = command.lowercase(Locale.ROOT)
        val candidates = timeToken.findAll(normalized).filter { match ->
            val suffix = match.groupValues[3]
            val prefix = normalized.substring(0, match.range.first)
            val hasCue = suffix.isNotBlank() || timePrefix.containsMatchIn(prefix)
            val following = normalized.substring(match.range.last + 1)
            hasCue && !durationSuffix.containsMatchIn(following)
        }.toList()
        // Multiple explicit times are ambiguous; never silently choose one.
        if (candidates.size != 1) return null

        val match = candidates.single()
        var hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].ifBlank { "0" }.toIntOrNull() ?: return null
        if (minute !in 0..59 || hour !in 0..23) return null

        val isPm = normalized.contains("pm") || normalized.contains("दोपहर") ||
            normalized.contains("शाम") || normalized.contains("रात")
        val isAm = normalized.contains("am") || normalized.contains("सुबह") ||
            normalized.contains("सवेरे") || normalized.contains("भोर")
        if (isPm && hour in 1..11) hour += 12
        if (isAm && hour == 12) hour = 0
        if (normalized.contains("रात") && hour == 12) hour = 0
        return hour * 60 + minute
    }
}

object TimerDurationParser {
    fun parseSeconds(command: String): Int? {
        val amount = Regex("(\\d+)").find(command)?.groupValues?.get(1)?.toLongOrNull() ?: return null
        val lower = command.lowercase(Locale.ROOT)
        val multiplier = when {
            lower.contains("hour") || lower.contains("घंटा") -> 3600L
            lower.contains("minute") || lower.contains("मिनट") -> 60L
            lower.contains("second") || lower.contains("सेकंड") -> 1L
            else -> 60L
        }
        if (amount > 86400L / multiplier) return null
        return (amount * multiplier).toInt().takeIf { it > 0 }
    }
}
