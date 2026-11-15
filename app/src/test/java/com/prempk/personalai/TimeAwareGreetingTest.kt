package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeAwareGreetingTest {
    @Test fun mapsLocalClockHoursToGreetingPeriods() {
        assertEquals(TimeAwareGreeting.Period.NIGHT, TimeAwareGreeting.period(0))
        assertEquals(TimeAwareGreeting.Period.NIGHT, TimeAwareGreeting.period(4))
        assertEquals(TimeAwareGreeting.Period.MORNING, TimeAwareGreeting.period(5))
        assertEquals(TimeAwareGreeting.Period.MORNING, TimeAwareGreeting.period(11))
        assertEquals(TimeAwareGreeting.Period.AFTERNOON, TimeAwareGreeting.period(12))
        assertEquals(TimeAwareGreeting.Period.AFTERNOON, TimeAwareGreeting.period(16))
        assertEquals(TimeAwareGreeting.Period.EVENING, TimeAwareGreeting.period(17))
        assertEquals(TimeAwareGreeting.Period.EVENING, TimeAwareGreeting.period(20))
        assertEquals(TimeAwareGreeting.Period.NIGHT, TimeAwareGreeting.period(21))
        assertEquals(TimeAwareGreeting.Period.NIGHT, TimeAwareGreeting.period(23))
    }

    @Test fun respondsToGreetingPhrasesInHindiAndEnglish() {
        assertEquals(AssistantCommandType.GREETING, CommandParser.parse("PK, Good Morning"))
        assertEquals(AssistantCommandType.GREETING, CommandParser.parse("शुभ संध्या"))
        assertEquals(AssistantCommandType.GREETING, CommandParser.parse("Good night"))
    }
}
