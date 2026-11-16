package com.prempk.personalai

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formats device-clock values in the language used for the spoken response. */
object VoiceDateTimeFormatter {
    data class SpokenDateTime(val time: String, val date: String)

    fun format(
        instant: Date,
        languageTag: String,
        timeZone: TimeZone = TimeZone.getDefault()
    ): SpokenDateTime {
        val locale = when (languageTag.substringBefore('-').lowercase(Locale.ROOT)) {
            "hi" -> Locale.forLanguageTag("hi-IN")
            "en" -> Locale.forLanguageTag("en-IN")
            else -> Locale.forLanguageTag("en-IN")
        }
        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale).apply {
            this.timeZone = timeZone.clone() as TimeZone
        }
        val dateFormatter = DateFormat.getDateInstance(DateFormat.FULL, locale).apply {
            this.timeZone = timeZone.clone() as TimeZone
        }
        return SpokenDateTime(
            time = timeFormatter.format(instant),
            date = dateFormatter.format(instant)
        )
    }
}
