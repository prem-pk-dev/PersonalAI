package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceTimeParsersTest {
    @Test fun parsesEnglishAndHindiAlarmTimes() {
        assertEquals(450, AlarmTimeParser.parseMinutes("set alarm at 7:30 AM"))
        assertEquals(1200, AlarmTimeParser.parseMinutes("रात 8 बजे अलार्म लगाओ"))
        assertEquals(1155, AlarmTimeParser.parseMinutes("शाम 7:15 का अलार्म"))
        assertEquals(780, AlarmTimeParser.parseMinutes("दोपहर 1 बजे"))
        assertEquals(0, AlarmTimeParser.parseMinutes("सुबह 12 बजे"))
        assertNull(AlarmTimeParser.parseMinutes("alarm at 7:75"))
        assertNull(AlarmTimeParser.parseMinutes("set alarm"))
    }

    @Test fun alarmParserRequiresAnExplicitTimeCue() {
        assertEquals(450, AlarmTimeParser.parseMinutes("set alarm at 7:30"))
        assertEquals(450, AlarmTimeParser.parseMinutes("alarm 7:30"))
        assertEquals(450, AlarmTimeParser.parseMinutes("tomorrow 12 March at 7:30 AM"))
        assertEquals(420, AlarmTimeParser.parseMinutes("सुबह 7 बजे अलार्म लगाओ"))
        assertNull(AlarmTimeParser.parseMinutes("set alarm in 5 minutes"))
        assertNull(AlarmTimeParser.parseMinutes("set alarm for 5 minutes"))
        assertNull(AlarmTimeParser.parseMinutes("set alarm at 5 minutes"))
    }

    @Test fun rejectsMultipleExplicitAlarmTimesInsteadOfGuessing() {
        assertNull(AlarmTimeParser.parseMinutes("set alarm at 7:30 or 8:30"))
        assertEquals(1_200, AlarmTimeParser.parseMinutes("रात 8 बजे अलार्म लगाओ"))
        assertEquals(0, AlarmTimeParser.parseMinutes("सुबह 12 बजे अलार्म लगाओ"))
        assertEquals(780, AlarmTimeParser.parseMinutes("दोपहर 1 बजे अलार्म लगाओ"))
    }

    @Test fun parsesTimerDurationsWithoutIntegerOverflow() {
        assertEquals(300, TimerDurationParser.parseSeconds("set timer for 5 minutes"))
        assertEquals(3600, TimerDurationParser.parseSeconds("1 hour timer"))
        assertEquals(45, TimerDurationParser.parseSeconds("45 seconds"))
        assertNull(TimerDurationParser.parseSeconds("90000 hours"))
        assertNull(TimerDurationParser.parseSeconds("2562047788015215 hours"))
        assertNull(TimerDurationParser.parseSeconds("timer please"))
        assertNull(TimerDurationParser.parseSeconds("5 minutes and 10 seconds"))
        assertNull(TimerDurationParser.parseSeconds("1 hour 30 minutes"))
        assertNull(TimerDurationParser.parseSeconds("0 seconds"))
    }
}
