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
    }

    @Test fun recognizesCommunicationAndAlarmCommands() {
        assertEquals(AssistantCommandType.CALL_CONTACT, CommandParser.parse("Rahul ko call karo"))
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
        assertEquals(AssistantCommandType.HELP, CommandParser.parse("help"))
    }

    @Test fun recognizesAlarmAndTimerCommands() {
        assertEquals(AssistantCommandType.SET_ALARM, CommandParser.parse("set alarm at 7:30"))
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
        assertEquals(AssistantCommandType.MEDIA_PLAY, CommandParser.parse("गाना चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("pause music"))
        assertEquals(AssistantCommandType.MEDIA_PAUSE, CommandParser.parse("गाना रोक दो"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("next song"))
        assertEquals(AssistantCommandType.MEDIA_NEXT, CommandParser.parse("अगला गाना चलाओ"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("previous track"))
        assertEquals(AssistantCommandType.MEDIA_PREVIOUS, CommandParser.parse("पिछला गाना चलाओ"))
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
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("launch Spotify"))
        assertEquals(AssistantCommandType.OPEN_INSTALLED_APP, CommandParser.parse("कैलकुलेटर खोलो"))
        assertEquals("calculator", InstalledAppCommandParser.extractAppName("open calculator"))
        assertEquals("Spotify", InstalledAppCommandParser.extractAppName("Hey PK launch Spotify"))
        assertEquals("कैलकुलेटर", InstalledAppCommandParser.extractAppName("कैलकुलेटर खोलो"))
    }

}
