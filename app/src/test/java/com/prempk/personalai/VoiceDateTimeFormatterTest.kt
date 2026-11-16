package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class VoiceDateTimeFormatterTest {
    private val fixedInstant = Date(1_794_868_200_000L) // 2026-11-16T22:30:00Z

    @Test fun formatsTimeUsingTheRequestedLanguageLocale() {
        listOf("en-IN" to Locale.forLanguageTag("en-IN"), "hi-IN" to Locale.forLanguageTag("hi-IN"))
            .forEach { (tag, locale) ->
                val zone = TimeZone.getTimeZone("UTC")
                val actual = VoiceDateTimeFormatter.format(fixedInstant, tag, zone)
                val expected = DateFormat.getTimeInstance(DateFormat.SHORT, locale).apply {
                    timeZone = zone
                }.format(fixedInstant)
                assertTrue(actual.time.isNotBlank())
                assertEquals(expected, actual.time)
            }
    }

    @Test fun formatsFullDateInTheRequestedLanguageLocale() {
        listOf("en-IN" to Locale.forLanguageTag("en-IN"), "hi-IN" to Locale.forLanguageTag("hi-IN"))
            .forEach { (tag, locale) ->
                val zone = TimeZone.getTimeZone("UTC")
                val actual = VoiceDateTimeFormatter.format(fixedInstant, tag, zone)
                val expected = DateFormat.getDateInstance(DateFormat.FULL, locale).apply {
                    timeZone = zone
                }.format(fixedInstant)
                assertTrue(actual.date.isNotBlank())
                assertEquals(expected, actual.date)
            }
    }

    @Test fun appliesTheDeviceTimezoneToBothTimeAndDate() {
        val utc = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("UTC")
        )
        val india = VoiceDateTimeFormatter.format(
            fixedInstant, "en-IN", TimeZone.getTimeZone("Asia/Kolkata")
        )
        assertTrue(utc.time != india.time || utc.date != india.date)
        assertTrue(india.date.contains("17") || india.date.contains("17"))
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
