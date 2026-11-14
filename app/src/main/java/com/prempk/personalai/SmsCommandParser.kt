package com.prempk.personalai

import java.util.Locale

data class SmsRequest(
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val body: String = ""
)

/** Parses explicit recipient-based SMS commands without attempting to send anything. */
object SmsCommandParser {
    private val numberPattern = Regex("\\+?[0-9][0-9\\s-]{7,}")
    private val contactPattern = Regex(
        "^(.+?)\\s+(?:ko|को)\\s+(?:sms|message|मैसेज|संदेश)\\s+(?:bhejo|bhej\\s+do|karo|भेजो|करो)(?:\\s+(.+))?$",
        RegexOption.IGNORE_CASE
    )

    fun parse(raw: String): SmsRequest? {
        val command = raw.trim().replace(Regex("(?i)^hey assistant[, ]*"), "")
        val numberMatch = numberPattern.find(command)
        if (numberMatch != null) {
            val number = numberMatch.value.replace(Regex("[\\s-]"), "")
            val body = command.substring(numberMatch.range.last + 1)
                .trim().replace(Regex("(?i)^(message|sms)\\s*"), "")
                .replace(Regex("^(मैसेज|संदेश)\\s*"), "")
            return SmsRequest(phoneNumber = number, body = body)
        }

        val match = contactPattern.matchEntire(command) ?: return null
        val name = match.groupValues[1].trim().trim(',', '.', '!')
        val body = match.groupValues.getOrNull(2).orEmpty().trim()
        if (name.isBlank()) return null
        return SmsRequest(contactName = name, body = body)
    }
}
