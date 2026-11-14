package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceLanguageTest {
    @Test fun detectsHindiFromDevanagariScript() {
        assertEquals("hi-IN", VoiceLanguage.detect("अभी कितने बजे हैं"))
        assertEquals("hi-IN", VoiceLanguage.detect("यूट्यूब खोलो"))
    }

    @Test fun defaultsLatinAndEnglishInputToEnglishIndia() {
        assertEquals("en-IN", VoiceLanguage.detect("What time is it?"))
        assertEquals("en-IN", VoiceLanguage.detect("YouTube kholo"))
    }
}
