package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsCommandParserTest {
    @Test fun parsesHindiEnglishContactRecipientExamples() {
        assertEquals("Rahul", SmsCommandParser.parse("Rahul ko SMS bhejo")?.contactName)
        assertEquals("Papa", SmsCommandParser.parse("Papa ko message bhejo")?.contactName)
        assertEquals("Amit", SmsCommandParser.parse("Amit ko SMS karo")?.contactName)
    }

    @Test fun parsesPhoneNumberAndOptionalBody() {
        val request = SmsCommandParser.parse("SMS 9876543210 hello there")
        assertEquals("9876543210", request?.phoneNumber)
        assertEquals("hello there", request?.body)
    }

    @Test fun parsesOptionalBodyAfterContactCommand() {
        val request = SmsCommandParser.parse("Rahul ko SMS bhejo kal milte hain")
        assertEquals("Rahul", request?.contactName)
        assertEquals("kal milte hain", request?.body)
    }

    @Test fun rejectsUnclearRecipientCommands() {
        assertNull(SmsCommandParser.parse("SMS bhejo"))
        assertNull(SmsCommandParser.parse(""))
    }
}
