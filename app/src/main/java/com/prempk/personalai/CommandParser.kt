package com.prempk.personalai

import java.util.Locale

enum class AssistantCommandType {
    OPEN_WHATSAPP,
    COMPOSE_WHATSAPP,
    OPEN_YOUTUBE,
    OPEN_CHROME,
    OPEN_SETTINGS,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_INSTALLED_APP,
    MEDIA_PLAY,
    MEDIA_PAUSE,
    MEDIA_NEXT,
    MEDIA_PREVIOUS,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    CALL_CONTACT,
    COMPOSE_SMS,
    OPEN_ALARMS,
    SET_ALARM,
    SET_TIMER,
    CREATE_REMINDER,
    OPEN_MAPS,
    INFO_TIME,
    INFO_DATE,
    INFO_BATTERY,
    GREETING,
    SCROLL_UP,
    SCROLL_DOWN,
    GO_BACK,
    GO_HOME,
    HELP,
    UNKNOWN
}

/** Extracts the user-authored message body from a WhatsApp compose command. */
object WhatsAppMessageParser {
    fun extractBody(raw: String): String = raw
        .replace(Regex("(?i)\\b(hey\\s+pk|whatsapp|message|msg|text|send|please)\\b"), " ")
        .replace(Regex("(?i)\\b(bhejo|bhej do|karo|kar do|par|pe)\\b"), " ")
        .replace(Regex("(व्हाट्सएप|व्हाट्सऐप|मैसेज|संदेश|भेजो|भेज दो|करो|कर दो|पर|पे)"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

/** Extracts an installed app's spoken display name from a launch command. */
object InstalledAppCommandParser {
    fun extractAppName(raw: String): String = raw.trim()
        .replace(Regex("(?i)^\\s*(hey\\s+pk|pk)\\s+"), "")
        .replace(Regex("^(पीके|पी\\s+के)\\s*"), "")
        .replace(Regex("(?i)^\\s*(please\\s+)?(open|launch|start)\\s+"), "")
        .replace(Regex("(?i)\\s+(app\\s+)?(please\\s+)?(open|launch|start)$"), "")
        .replace(Regex("(?i)(\\s+app)?\\s*(ko\\s*)?(kholo|kholna|chalao|shuru\\s+karo)$"), "")
        .replace(Regex("^(कृपया\\s*)?"), "")
        .replace(Regex("(\\s+ऐप)?\\s*(को\\s*)?(खोलो|खोलना|चलाओ|शुरू करो)$"), "")
        .trim()
}

/** Deterministic, Android-independent command classification for the MVP. */
object CommandParser {
    fun parse(raw: String): AssistantCommandType {
        val command = raw.trim().lowercase(Locale.ROOT)
            .replace(Regex("(?i)^\\s*(hey\\s+pk|pk)\\s+"), "")
            .replace(Regex("^(पीके|पी\\s+के)\\s*"), "")
            .trim()
        return when {
            containsAny(command, "whatsapp", "व्हाट्सएप") &&
                containsAny(command, "message", "msg", "text", "send", "bhejo", "भेजो", "मैसेज", "संदेश") ->
                AssistantCommandType.COMPOSE_WHATSAPP
            containsAny(command, "whatsapp", "व्हाट्सएप") -> AssistantCommandType.OPEN_WHATSAPP
            containsAny(command, "youtube", "यूट्यूब") -> AssistantCommandType.OPEN_YOUTUBE
            containsAny(command, "chrome", "क्रोम", "browser", "ब्राउज़र") -> AssistantCommandType.OPEN_CHROME
            containsAny(command, "wifi settings", "wifi setting", "wi-fi settings", "wi-fi setting", "wireless settings", "wireless setting", "वाईफाई सेटिंग", "वाई-फाई सेटिंग", "वाइफाइ सेटिंग", "वाईफाई सेटिंग्स", "वाई-फाई सेटिंग्स", "वाइफाइ सेटिंग्स") -> AssistantCommandType.OPEN_WIFI_SETTINGS
            containsAny(command, "bluetooth settings", "bluetooth setting", "bluetooth options", "ब्लूटूथ सेटिंग", "ब्लूटूथ सेटिंग्स") -> AssistantCommandType.OPEN_BLUETOOTH_SETTINGS
            containsAny(command, "settings", "सेटिंग") -> AssistantCommandType.OPEN_SETTINGS
            containsAny(command, "sms", "message", "मैसेज", "संदेश") -> AssistantCommandType.COMPOSE_SMS
            containsAny(command, "timer", "टाइमर") -> AssistantCommandType.SET_TIMER
            containsAny(command, "remind me", "reminder", "याद दिला", "रिमाइंडर", "रिमाइंड") -> AssistantCommandType.CREATE_REMINDER
            containsAny(command, "call", "कॉल", "फोन लगाओ", "फोन करो") -> AssistantCommandType.CALL_CONTACT
            containsAny(command, "alarm", "अलार्म") && containsAny(command, "set", "लगाओ", "लगाना", "बजा", "बजे") -> AssistantCommandType.SET_ALARM
            containsAny(command, "alarm", "अलार्म") -> AssistantCommandType.OPEN_ALARMS
            containsAny(command, "map", "maps", "नक्शा", "मैप") -> AssistantCommandType.OPEN_MAPS
            containsAny(command, "time", "समय", "samay", "kitne baje", "कितने बजे") -> AssistantCommandType.INFO_TIME
            containsAny(command, "date", "tareekh", "tarikh", "तारीख", "आज कौन सा दिन") -> AssistantCommandType.INFO_DATE
            containsAny(command, "battery", "बैटरी", "चार्ज") -> AssistantCommandType.INFO_BATTERY
            containsAny(command, "volume up", "increase volume", "turn volume up", "volume badhao", "volume badha", "awaz badhao", "awaaz badhao", "आवाज बढ़ा", "वॉल्यूम बढ़ा", "आवाज़ तेज", "आवाज तेज") -> AssistantCommandType.VOLUME_UP
            containsAny(command, "volume down", "decrease volume", "turn volume down", "volume kam", "awaz kam", "awaaz kam", "आवाज कम", "वॉल्यूम कम", "आवाज़ धीमी", "आवाज धीमी") -> AssistantCommandType.VOLUME_DOWN
            containsAny(command, "mute volume", "mute sound", "awaz band", "awaaz band", "आवाज म्यूट", "वॉल्यूम म्यूट", "आवाज बंद") -> AssistantCommandType.VOLUME_MUTE
            containsAny(command, "next song", "next track", "skip song", "agla gana", "agla gaana", "अगला गाना", "अगला ट्रैक", "नेक्स्ट गाना", "नेक्स्ट ट्रैक") -> AssistantCommandType.MEDIA_NEXT
            containsAny(command, "previous song", "previous track", "pichla gana", "pichla gaana", "पिछला गाना", "पिछला ट्रैक", "प्रिवियस गाना", "पिछला") -> AssistantCommandType.MEDIA_PREVIOUS
            containsAny(command, "pause music", "pause song", "music pause", "gana roko", "gaana roko", "music roko", "गाना रोक", "म्यूजिक रोक", "पॉज करो") -> AssistantCommandType.MEDIA_PAUSE
            containsAny(command, "play music", "play song", "resume music", "music play", "music play karo", "play music karo", "gana chalao", "gaana chalao", "music chalao", "गाना चलाओ", "म्यूजिक चलाओ", "फिर से चलाओ") -> AssistantCommandType.MEDIA_PLAY
            containsAny(command, "scroll up", "page up", "upar scroll", "upar scrol", "ऊपर स्क्रोल", "ऊपर स्क्रॉल", "ऊपर करो") -> AssistantCommandType.SCROLL_UP
            containsAny(command, "scroll down", "page down", "neeche scroll", "neeche scrol", "नीचे स्क्रोल", "नीचे स्क्रॉल", "नीचे करो") -> AssistantCommandType.SCROLL_DOWN
            containsAny(command, "go back", "back", "peeche jao", "wapas jao", "पीछे जाओ", "वापस जाओ") -> AssistantCommandType.GO_BACK
            containsAny(command, "go home", "home screen", "home jao", "होम स्क्रीन", "होम जाओ") -> AssistantCommandType.GO_HOME
            containsAny(command, "good morning", "good afternoon", "good evening", "good night", "सुप्रभात", "शुभ प्रभात", "शुभ दोपहर", "शुभ संध्या", "शुभ रात्रि", "नमस्कार") -> AssistantCommandType.GREETING
            containsAny(command, "help", "मदद", "क्या कर सकते") -> AssistantCommandType.HELP
            containsAny(command, "open ", "launch ", "start ", "kholo", "kholna", "chalao", "shuru karo", "खोलो", "खोलना", "चलाओ") -> AssistantCommandType.OPEN_INSTALLED_APP
            else -> AssistantCommandType.UNKNOWN
        }
    }

    private fun containsAny(command: String, vararg terms: String): Boolean =
        terms.any(command::contains)
}
