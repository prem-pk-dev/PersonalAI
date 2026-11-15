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
    OPEN_MAPS,
    INFO_TIME,
    INFO_DATE,
    INFO_BATTERY,
    GREETING,
    HELP,
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
            containsAny(command, "map", "maps", "नक्शा", "मैप") -> AssistantCommandType.OPEN_MAPS
            containsAny(command, "time", "समय", "कितने बजे") -> AssistantCommandType.INFO_TIME
            containsAny(command, "date", "तारीख", "आज कौन सा दिन") -> AssistantCommandType.INFO_DATE
            containsAny(command, "battery", "बैटरी", "चार्ज") -> AssistantCommandType.INFO_BATTERY
            containsAny(command, "good morning", "good afternoon", "good evening", "good night", "सुप्रभात", "शुभ प्रभात", "शुभ दोपहर", "शुभ संध्या", "शुभ रात्रि", "नमस्कार") -> AssistantCommandType.GREETING
            containsAny(command, "help", "मदद", "क्या कर सकते") -> AssistantCommandType.HELP
            else -> AssistantCommandType.UNKNOWN
        }
    }

    private fun containsAny(command: String, vararg terms: String): Boolean =
        terms.any(command::contains)
}
