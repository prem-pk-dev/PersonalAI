package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {
    @Test fun recognizesEnglishAndHindiAppCommands() {
        assertEquals(AssistantCommandType.OPEN_WHATSAPP, CommandParser.parse("WhatsApp kholo"))
        assertEquals(AssistantCommandType.OPEN_WHATSAPP, CommandParser.parse("व्हाट्सएप खोलो"))
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

    @Test fun recognizesCrossAppAccessibilityCommands() {
        assertEquals(AssistantCommandType.SCROLL_UP, CommandParser.parse("scroll up"))
        assertEquals(AssistantCommandType.SCROLL_DOWN, CommandParser.parse("नीचे स्क्रॉल करो"))
        assertEquals(AssistantCommandType.GO_BACK, CommandParser.parse("go back"))
        assertEquals(AssistantCommandType.GO_HOME, CommandParser.parse("home screen"))
    }

    @Test fun trimsWhitespaceAndRejectsUnknownCommands() {
        assertEquals(AssistantCommandType.OPEN_SETTINGS, CommandParser.parse("  Settings kholo  "))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse("tell me a joke"))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse(""))
    }
}
