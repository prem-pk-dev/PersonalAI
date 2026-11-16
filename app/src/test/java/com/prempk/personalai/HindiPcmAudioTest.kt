package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HindiPcmAudioTest {
    @Test fun convertsSignedPcm16ToNormalizedFloat() {
        val result = HindiPcmAudio.toFloatSamples(
            shortArrayOf(Short.MIN_VALUE, -16384, 0, 16384, Short.MAX_VALUE)
        )
        assertEquals(-1f, result[0], 0.000001f)
        assertEquals(-0.5f, result[1], 0.000001f)
        assertEquals(0f, result[2], 0.000001f)
        assertEquals(0.5f, result[3], 0.000001f)
        assertEquals(Short.MAX_VALUE / 32768f, result[4], 0.000001f)
    }

    @Test fun respectsRequestedLengthAndIgnoresTrailingSamples() {
        val samples = shortArrayOf(16384, -16384, Short.MAX_VALUE)
        val result = HindiPcmAudio.toFloatSamples(samples, 2)
        assertEquals(2, result.size)
        assertEquals(0.5f, result[0], 0.000001f)
        assertEquals(-0.5f, result[1], 0.000001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroLength() {
        HindiPcmAudio.toFloatSamples(shortArrayOf(1), 0)
    }

    @Test fun rejectsLengthLargerThanInputForBothHelpers() {
        val samples = shortArrayOf(1, 2)
        assertThrows(IllegalArgumentException::class.java) {
            HindiPcmAudio.toFloatSamples(samples, 3)
        }
        assertThrows(IllegalArgumentException::class.java) {
            HindiPcmAudio.rms(samples, 3)
        }
    }

    @Test fun calculatesExactRmsForSilenceAndKnownSignal() {
        assertEquals(0f, HindiPcmAudio.rms(shortArrayOf(0, 0, 0)), 0.000001f)
        assertEquals(kotlin.math.sqrt(0.125).toFloat(),
            HindiPcmAudio.rms(shortArrayOf(0, 16384)), 0.000001f)
        assertTrue(HindiPcmAudio.rms(shortArrayOf(12000, -12000)) > 0.3f)
    }
}
