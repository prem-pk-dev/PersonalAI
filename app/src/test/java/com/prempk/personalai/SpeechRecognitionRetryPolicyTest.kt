package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpeechRecognitionRetryPolicyTest {
    @Test fun retriesUseIncreasingBoundedBackoff() {
        assertEquals(700L, SpeechRecognitionRetryPolicy.delayAfterFailure(1))
        assertEquals(1_400L, SpeechRecognitionRetryPolicy.delayAfterFailure(2))
        assertEquals(2_800L, SpeechRecognitionRetryPolicy.delayAfterFailure(3))
        assertEquals(5_000L, SpeechRecognitionRetryPolicy.delayAfterFailure(4))
    }

    @Test fun retryStopsAfterConfiguredLimit() {
        assertEquals(4, SpeechRecognitionRetryPolicy.maxRetries())
        assertNull(SpeechRecognitionRetryPolicy.delayAfterFailure(5))
    }

    @Test fun invalidFailureCountsDoNotScheduleRetries() {
        assertNull(SpeechRecognitionRetryPolicy.delayAfterFailure(0))
        assertNull(SpeechRecognitionRetryPolicy.delayAfterFailure(-1))
    }
}
