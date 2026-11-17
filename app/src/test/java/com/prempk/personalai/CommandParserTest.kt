package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {
    @Test fun recognizesEnglishAndHindiAppCommands() {
        assertEquals(AssistantCommandType.OPEN_WHATSAPP, CommandParser.parse("WhatsApp kholo"))
        assertEquals(AssistantCommandType.OPEN_WHATSAPP, CommandParser.parse("व्हाट्सएप खोलो"))
        assertEquals(AssistantCommandType.COMPOSE_WHATSAPP, CommandParser.parse("WhatsApp par message bhejo hello"))
        assertEquals(AssistantCommandType.COMPOSE_WHATSAPP, CommandParser.parse("व्हाट्सएप पर मैसेज भेजो नमस्ते"))
        assertEquals(AssistantCommandType.OPEN_YOUTUBE, CommandParser.parse("YouTube kholo"))
        assertEquals(AssistantCommandType.OPEN_YOUTUBE, CommandParser.parse("यूट्यूब खोलो"))
        assertEquals(AssistantCommandType.OPEN_CHROME, CommandParser.parse("Chrome kholo"))
        assertEquals(AssistantCommandType.OPEN_SETTINGS, CommandParser.parse("सेटिंग खोलो"))
        assertEquals(AssistantCommandType.OPEN_WIFI_SETTINGS, CommandParser.parse("open Wi-Fi settings"))
        assertEquals(AssistantCommandType.OPEN_WIFI_SETTINGS, CommandParser.parse("wifi setting kholo"))
        assertEquals(AssistantCommandType.OPEN_WIFI_SETTINGS, CommandParser.parse("Wi-Fi setting kholo"))
        assertEquals(AssistantCommandType.OPEN_WIFI_SETTINGS, CommandParser.parse("वाईफाई सेटिंग खोलो"))
        assertEquals(AssistantCommandType.OPEN_BLUETOOTH_SETTINGS, CommandParser.parse("Bluetooth settings kholo"))
        assertEquals(AssistantCommandType.OPEN_BLUETOOTH_SETTINGS, CommandParser.parse("bluetooth setting kholo"))
        assertEquals(AssistantCommandType.OPEN_BLUETOOTH_SETTINGS, CommandParser.parse("ब्लूटूथ सेटिंग खोलो"))
    }

    @Test fun recognizesCommunicationAndAlarmCommands() {
        assertEquals(AssistantCommandType.CALL_CONTACT, CommandParser.parse("Rahul ko call karo"))
        assertEquals(AssistantCommandType.CALL_CONTACT, CommandParser.parse("Rahul ko phone lagao"))
        assertEquals(AssistantCommandType.CALL_CONTACT, CommandParser.parse("dial Rahul"))
        assertEquals(AssistantCommandType.CALL_CONTACT, CommandParser.parse("राहुल को कॉल करो"))
        assertEquals(AssistantCommandType.COMPOSE_SMS, CommandParser.parse("SMS 9876543210"))
        assertEquals(AssistantCommandType.COMPOSE_SMS, CommandParser.parse("मैसेज भेजो"))
        assertEquals(AssistantCommandType.COMPOSE_SMS, CommandParser.parse("Rahul ko SMS bhejo"))
        assertEquals(AssistantCommandType.COMPOSE_SMS, CommandParser.parse("Papa ko message bhejo"))
        assertEquals(AssistantCommandType.COMPOSE_SMS, CommandParser.parse("Amit ko SMS karo"))
        assertEquals(AssistantCommandType.OPEN_ALARMS, CommandParser.parse("Alarm kholo"))
        assertEquals(AssistantCommandType.OPEN_ALARMS, CommandParser.parse("अलार्म खोलो"))
        assertEquals(AssistantCommandType.OPEN_MAPS, CommandParser.parse("Maps kholo"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("time kya hua"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("आज की तारीख बताओ"))
        assertEquals(AssistantCommandType.INFO_BATTERY, CommandParser.parse("battery kitni hai"))
        assertEquals(AssistantCommandType.VOLUME_UP, CommandParser.parse("volume up"))
        assertEquals(AssistantCommandType.VOLUME_UP, CommandParser.parse("आवाज बढ़ा दो"))
        assertEquals(AssistantCommandType.VOLUME_DOWN, CommandParser.parse("volume down"))
        assertEquals(AssistantCommandType.VOLUME_DOWN, CommandParser.parse("आवाज कम करो"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("mute volume"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("आवाज म्यूट करो"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("mute karo"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("PK mute karo"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("volume mute karo"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("awaz mute karo"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("आवाज़ बंद करो"))
        assertEquals(AssistantCommandType.HELP, CommandParser.parse("help"))
    }

    @Test fun recognizesAlarmAndTimerCommands() {
        assertEquals(AssistantCommandType.SET_ALARM, CommandParser.parse("set alarm at 7:30"))
        assertEquals(AssistantCommandType.SET_ALARM, CommandParser.parse("PK alarm lagao"))
        assertEquals(AssistantCommandType.SET_ALARM, CommandParser.parse("PK alarm 7 baje lagao"))
        assertEquals(AssistantCommandType.SET_ALARM, CommandParser.parse("अलार्म लगा दो"))
        assertEquals(AssistantCommandType.SET_TIMER, CommandParser.parse("set timer for 5 minutes"))
        assertEquals(AssistantCommandType.OPEN_ALARMS, CommandParser.parse("open alarms"))
    }

    @Test fun recognizesReminderCommands() {
        assertEquals(AssistantCommandType.CREATE_REMINDER, CommandParser.parse("remind me to call Rahul"))
        assertEquals(AssistantCommandType.CREATE_REMINDER, CommandParser.parse("मुझे दवाई याद दिलाना"))
    }

    @Test fun recognizesCrossAppAccessibilityCommands() {
        assertEquals(AssistantCommandType.SCROLL_UP, CommandParser.parse("scroll up"))
        assertEquals(AssistantCommandType.SCROLL_DOWN, CommandParser.parse("नीचे स्क्रॉल करो"))
        assertEquals(AssistantCommandType.GO_BACK, CommandParser.parse("go back"))
        assertEquals(AssistantCommandType.GO_HOME, CommandParser.parse("home screen"))
    }

    @Test fun recognizesMediaPlaybackCommands() {
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("play music"))
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("music play karo"))
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("pk music play karo"))
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("पीके म्यूजिक चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("गाना चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("pause music"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("गाना रोक दो"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("next song"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("अगला गाना चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("अगला ट्रैक"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("previous track"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("पिछला गाना"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("पिछला गाना चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("पिछला ट्रैक"))
    }

    @Test fun recognizesCommonHinglishPlaybackVolumeAndNavigationCommands() {
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("gaana chalao"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("music roko"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("agla gaana"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("pichla gana"))
        assertEquals(AssistantCommandType.VOLUME_UP, CommandParser.parse("volume badhao"))
        assertEquals(AssistantCommandType.VOLUME_DOWN, CommandParser.parse("awaz kam karo"))
        assertEquals(AssistantCommandType.VOLUME_MUTE, CommandParser.parse("awaz band"))
        assertEquals(AssistantCommandType.SCROLL_UP, CommandParser.parse("upar scroll"))
        assertEquals(AssistantCommandType.SCROLL_DOWN, CommandParser.parse("neeche scroll"))
        assertEquals(AssistantCommandType.GO_BACK, CommandParser.parse("peeche jao"))
        assertEquals(AssistantCommandType.GO_HOME, CommandParser.parse("home jao"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("samay kya hai"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("abhi kitne baje hain"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("tell me the time"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("समय बताओ"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("aaj ki tareekh"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("aaj kya date hai"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("what is the date"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("आज क्या तारीख है"))
    }

    @Test fun recognizesExpandedEverydayNaturalPhrases() {
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("what's the time"))
        assertEquals(AssistantCommandType.INFO_TIME, CommandParser.parse("abhi time kya hai"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("which day is today"))
        assertEquals(AssistantCommandType.INFO_DATE, CommandParser.parse("aaj kaun sa din hai"))
        assertEquals(AssistantCommandType.INFO_BATTERY, CommandParser.parse("how much battery"))
        assertEquals(AssistantCommandType.INFO_BATTERY, CommandParser.parse("kitni battery bachi hai"))
        assertEquals(AssistantCommandType.VOLUME_UP, CommandParser.parse("make it louder"))
        assertEquals(AssistantCommandType.VOLUME_UP, CommandParser.parse("awaz tez karo"))
        assertEquals(AssistantCommandType.VOLUME_DOWN, CommandParser.parse("make it quieter"))
        assertEquals(AssistantCommandType.VOLUME_DOWN, CommandParser.parse("awaz dheemi karo"))
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("start playing music"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("stop the music"))
    }

    @Test fun trimsWhitespaceAndRejectsUnknownCommands() {
        assertEquals(AssistantCommandType.OPEN_SETTINGS, CommandParser.parse("  Settings kholo  "))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse("tell me a joke"))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse(""))
    }
    @Test fun extractsWhatsAppMessageBodyInEnglishAndHindi() {
        assertEquals("hello Rahul", WhatsAppMessageParser.extractBody("Hey PK WhatsApp par message bhejo hello Rahul"))
        assertEquals("नमस्ते राहुल", WhatsAppMessageParser.extractBody("व्हाट्सएप पर मैसेज भेजो नमस्ते राहुल"))
        assertEquals("कल मिलते हैं", WhatsAppMessageParser.extractBody("व्हाट्सएप पर संदेश भेजो कल मिलते हैं"))
        assertEquals("", WhatsAppMessageParser.extractBody("WhatsApp message bhejo"))
    }

    @Test fun recognizesGenericInstalledAppLaunchCommands() {
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("open calculator"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("show calculator"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("PK please open Instagram"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("calculator dikhao"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("camera khol do"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("gallery dikha do"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("फोन खोलो"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("फोटो दिखाओ"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("launch Spotify"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("कैलकुलेटर खोलो"))
        assertEquals("calculator", InstalledAppCommandParser.extractAppName("open calculator"))
        assertEquals("calculator", InstalledAppCommandParser.extractAppName("calculator dikhao"))
        assertEquals("camera", InstalledAppCommandParser.extractAppName("camera khol do"))
        assertEquals("gallery", InstalledAppCommandParser.extractAppName("gallery dikha do"))
        assertEquals("Instagram", InstalledAppCommandParser.extractAppName("PK please open Instagram"))
        assertEquals("फोन", InstalledAppCommandParser.extractAppName("फोन खोलो"))
        assertEquals("फोटो", InstalledAppCommandParser.extractAppName("फोटो दिखाओ"))
        assertEquals("Spotify", InstalledAppCommandParser.extractAppName("Hey PK launch Spotify"))
        assertEquals("chatgpt", InstalledAppCommandParser.extractAppName("pk chatgpt kholo"))
        assertEquals("chatgpt", InstalledAppCommandParser.extractAppName("पीके chatgpt खोलो"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("pk chatgpt kholo"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("पीके chatgpt खोलो"))
        assertEquals("कैलकुलेटर", InstalledAppCommandParser.extractAppName("कैलकुलेटर खोलो"))
    }

}
