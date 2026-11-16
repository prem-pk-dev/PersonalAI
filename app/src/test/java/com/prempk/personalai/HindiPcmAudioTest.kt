package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HindiPcmAudioTest {
    @Test fun convertsSignedPcm16ToNormalizedFloat() {
        val result = HindiPcmAudio.toFloatSamples(shortArrayOf(Short.MIN_VALUE, 0, Short.MAX_VALUE))
        assertEquals(-1f, result[0], 0.0001f)
        assertEquals(0f, result[1], 0.0001f)
        assertTrue(result[2] > 0.99f && result[2] <= 1f)
    }

    @Test fun respectsRequestedLength() {
        val result = HindiPcmAudio.toFloatSamples(shortArrayOf(1000, 2000, 3000), 2)
        assertEquals(2, result.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidLength() {
        HindiPcmAudio.toFloatSamples(shortArrayOf(1), 0)
    }

    @Test fun calculatesRmsForSilenceAndSignal() {
        assertEquals(0f, HindiPcmAudio.rms(shortArrayOf(0, 0, 0)), 0.0001f)
        assertTrue(HindiPcmAudio.rms(shortArrayOf(12000, -12000)) > 0.3f)
    }
}
