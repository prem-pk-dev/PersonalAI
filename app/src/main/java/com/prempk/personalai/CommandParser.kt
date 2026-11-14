package com.prempk.personalai

import java.util.Locale

enum class AssistantCommandType {
    OPEN_WHATSAPP,
    OPEN_YOUTUBE,
    OPEN_CHROME,
    OPEN_SETTINGS,
    CALL_CONTACT,
    COMPOSE_SMS,
    OPEN_ALARMS,
    UNKNOWN
}

/** Deterministic, Android-independent command classification for the MVP. */
object CommandParser {
    fun parse(raw: String): AssistantCommandType {
        val command = raw.trim().lowercase(Locale.ROOT)
        return when {
            containsAny(command, "whatsapp", "व्हाट्सएप") -> AssistantCommandType.OPEN_WHATSAPP
            containsAny(command, "youtube", "यूट्यूब") -> AssistantCommandType.OPEN_YOUTUBE
            containsAny(command, "chrome", "क्रोम", "browser", "ब्राउज़र") -> AssistantCommandType.OPEN_CHROME
            containsAny(command, "settings", "सेटिंग") -> AssistantCommandType.OPEN_SETTINGS
            containsAny(command, "call", "कॉल", "फोन लगाओ", "फोन करो") -> AssistantCommandType.CALL_CONTACT
            containsAny(command, "sms", "message", "मैसेज", "संदेश") -> AssistantCommandType.COMPOSE_SMS
            containsAny(command, "alarm", "अलार्म") -> AssistantCommandType.OPEN_ALARMS
            else -> AssistantCommandType.UNKNOWN
        }
    }

    private fun containsAny(command: String, vararg terms: String): Boolean =
        terms.any(command::contains)
}
