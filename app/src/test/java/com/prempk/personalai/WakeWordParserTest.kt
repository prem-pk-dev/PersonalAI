package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WakeWordParserTest {
    @Test fun extractsCommandAfterEnglishPkWakeWord() {
        assertEquals("youtube kholo", WakeWordParser.extractCommand("PK YouTube kholo"))
    }

    @Test fun acceptsHindiSpeechRecognitionVariants() {
        assertEquals("papa ko call karo", WakeWordParser.extractCommand("पीके पापा को कॉल करो"))
        assertEquals("rahul ko sms bhejo", WakeWordParser.extractCommand("पी के Rahul ko SMS bhejo"))
    }

    @Test fun returnsEmptyCommandWhenOnlyWakeWordWasSpoken() {
        assertEquals("", WakeWordParser.extractCommand("PK"))
    }

    @Test fun doesNotMatchPkInsideAnotherWord() {
        assertNull(WakeWordParser.extractCommand("speak to assistant"))
        assertNull(WakeWordParser.extractCommand("package manager"))
    }
}
