package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.TimeZone

class VoiceDateTimeFormatterTest {
    private val fixedInstant = Date(1_794_868_200_000L) // 2026-11-16T12:30:00Z

    @Test fun formatsTimeUsingTheRequestedLanguageLocale() {
        val english = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("UTC")
        )
        val hindi = VoiceDateTimeFormatter.format(
            fixedInstant, "hi-IN", TimeZone.getTimeZone("UTC")
        )
        assertTrue(english.time.isNotBlank())
        assertTrue(hindi.time.isNotBlank())
        assertNotEquals(english.time, hindi.time)
    }

    @Test fun formatsFullDateInTheRequestedLanguageLocale() {
        val english = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("UTC")
        )
        val hindi = VoiceDateTimeFormatter.format(
            fixedInstant, "hi-IN", TimeZone.getTimeZone("UTC")
        )
        assertTrue(english.date.isNotBlank())
        assertTrue(hindi.date.isNotBlank())
        assertNotEquals(english.date, hindi.date)
    }

    @Test fun appliesTheDeviceTimezoneToBothTimeAndDate() {
        val utc = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("UTC")
        )
        val india = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("Asia/Kolkata")
        )
        assertNotEquals(utc.time, india.time)
        assertNotEquals(utc.date, india.date)
    }

    @Test fun unsupportedLanguageTagsFallBackToEnglishIndia() {
        val unsupported = VoiceDateTimeFormatter.format(
            fixedInstant, "fr-FR", TimeZone.getTimeZone("UTC")
        )
        val english = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("UTC")
        )
        assertEquals(english, unsupported)
    }
}
