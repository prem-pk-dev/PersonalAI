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
        assertEquals(null, VoiceLanguage.fromLanguageTag(null))
    }


    @Test fun explicitRecognizerLanguageTakesPriorityOverTranscriptHeuristic() {
        assertEquals("en-IN", VoiceLanguage.resolve("YouTube kholo", "en-US"))
        assertEquals("hi-IN", VoiceLanguage.resolve("Open YouTube", "hi-IN"))
        assertEquals("hi-IN", VoiceLanguage.resolve("YouTube kholo", null))
        assertEquals("en-IN", VoiceLanguage.resolve("Open YouTube", null))
    }

    @Test fun ignoresUnsupportedRecognizerLanguageAndUsesTranscriptFallback() {
        assertEquals("hi-IN", VoiceLanguage.resolve("YouTube kholo", "fr-FR"))
    }

    @Test fun defaultsLatinAndEnglishInputToEnglishIndia() {
        assertEquals("en-IN", VoiceLanguage.detect("What time is it?"))
        assertEquals("hi-IN", VoiceLanguage.detect("YouTube kholo"))
        assertEquals("hi-IN", VoiceLanguage.detect("abhi kitne baje hain"))
    }
}
