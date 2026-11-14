package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceLanguageTest {
    @Test fun detectsHindiFromDevanagariScript() {
        assertEquals("hi-IN", VoiceLanguage.detect("अभी कितने बजे हैं"))
        assertEquals("hi-IN", VoiceLanguage.detect("यूट्यूब खोलो"))
    }

    @Test fun mapsAndroidLanguageDetectionTags() {
        assertEquals("hi-IN", VoiceLanguage.fromLanguageTag("hi-IN"))
        assertEquals("hi-IN", VoiceLanguage.fromLanguageTag("hi"))
        assertEquals("en-IN", VoiceLanguage.fromLanguageTag("en-US"))
        assertEquals("en-IN", VoiceLanguage.fromLanguageTag(null))
    }

    @Test fun defaultsLatinAndEnglishInputToEnglishIndia() {
        assertEquals("en-IN", VoiceLanguage.detect("What time is it?"))
        assertEquals("hi-IN", VoiceLanguage.detect("YouTube kholo"))
        assertEquals("hi-IN", VoiceLanguage.detect("abhi kitne baje hain"))
    }
}
