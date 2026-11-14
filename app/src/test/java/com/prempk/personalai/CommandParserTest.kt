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
    }

    @Test fun trimsWhitespaceAndRejectsUnknownCommands() {
        assertEquals(AssistantCommandType.OPEN_SETTINGS, CommandParser.parse("  Settings kholo  "))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse("tell me a joke"))
        assertEquals(AssistantCommandType.UNKNOWN, CommandParser.parse(""))
    }
}
