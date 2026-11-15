package com.prempk.personalai

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Offline greeting selected from the device's current local clock and timezone. */
object TimeAwareGreeting {
    enum class Period { MORNING, AFTERNOON, EVENING, NIGHT }

    fun period(hourOfDay: Int): Period {
        require(hourOfDay in 0..23)
        return when (hourOfDay) {
            in 5..11 -> Period.MORNING
            in 12..16 -> Period.AFTERNOON
            in 17..20 -> Period.EVENING
            else -> Period.NIGHT
        }
    }

    fun reply(
        isHindi: Boolean,
        now: Calendar = Calendar.getInstance(),
        locale: Locale = if (isHindi) Locale.forLanguageTag("hi-IN") else Locale.forLanguageTag("en-IN")
    ): String {
        val time = DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(now.timeInMillis))
        val greeting = if (isHindi) {
            when (period(now.get(Calendar.HOUR_OF_DAY))) {
                Period.MORNING -> "सुप्रभात"
                Period.AFTERNOON -> "शुभ दोपहर"
                Period.EVENING -> "शुभ संध्या"
                Period.NIGHT -> "शुभ रात्रि"
            }
        } else {
            when (period(now.get(Calendar.HOUR_OF_DAY))) {
                Period.MORNING -> "Good morning"
                Period.AFTERNOON -> "Good afternoon"
                Period.EVENING -> "Good evening"
                Period.NIGHT -> "Good night"
            }
        }
        return if (isHindi) "अभी $time बजे हैं। $greeting! बताइए, मैं आपकी क्या मदद करूँ?"
        else "It's $time. $greeting! How can I help you?"
    }
}
