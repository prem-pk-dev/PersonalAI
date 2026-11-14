package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WakeWordParserTest {
    @Test fun extractsCommandAfterEnglishPkWakeWord() {
        assertEquals("YouTube kholo", WakeWordParser.extractCommand("PK YouTube kholo"))
    }

    @Test fun acceptsHindiSpeechRecognitionVariants() {
        assertEquals("पापा को कॉल करो", WakeWordParser.extractCommand("पीके पापा को कॉल करो"))
        assertEquals("Rahul ko SMS bhejo", WakeWordParser.extractCommand("पी के Rahul ko SMS bhejo"))
    }

    @Test fun returnsEmptyCommandWhenOnlyWakeWordWasSpoken() {
        assertEquals("", WakeWordParser.extractCommand("PK"))
    }

    @Test fun doesNotTreatHeyAssistantAsWakeWord() {\n        assertNull(WakeWordParser.extractCommand("Hey Assistant, open YouTube"))\n    }\n\n    @Test fun doesNotMatchPkInsideAnotherWord() {
        assertNull(WakeWordParser.extractCommand("speak to assistant"))
        assertNull(WakeWordParser.extractCommand("package manager"))
    }
}
