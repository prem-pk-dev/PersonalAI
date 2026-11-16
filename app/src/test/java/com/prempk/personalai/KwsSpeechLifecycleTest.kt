package com.prempk.personalai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KwsSpeechLifecycleTest {
    @Test fun mediaAndVolumeResponsesWaitForSpeechCompletion() {
        val spokenCommands = listOf(
            AssistantCommandType.MEDIA_PLAY,
            AssistantCommandType.MEDIA_PAUSE,
            AssistantCommandType.MEDIA_NEXT,
            AssistantCommandType.MEDIA_PREVIOUS,
            AssistantCommandType.VOLUME_UP,
            AssistantCommandType.VOLUME_DOWN,
            AssistantCommandType.VOLUME_MUTE
        )
        spokenCommands.forEach { assertTrue("$it should wait for TTS", KwsSpeechLifecycle.shouldResumeAfterSpeech(it, true)) }
    }

    @Test fun existingSpokenCommandsKeepTheirDeferredResumeBehavior() {
        val spokenCommands = listOf(
            AssistantCommandType.INFO_TIME,
            AssistantCommandType.INFO_DATE,
            AssistantCommandType.GREETING,
            AssistantCommandType.INFO_BATTERY,
            AssistantCommandType.HELP,
            AssistantCommandType.UNKNOWN,
            AssistantCommandType.SCROLL_UP,
            AssistantCommandType.SCROLL_DOWN,
            AssistantCommandType.GO_BACK,
            AssistantCommandType.GO_HOME
        )
        spokenCommands.forEach { assertTrue("$it should wait for TTS", KwsSpeechLifecycle.shouldResumeAfterSpeech(it, true)) }
    }

    @Test fun doesNotScheduleListenerWhenOptInIsOff() {
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterSpeech(AssistantCommandType.MEDIA_PLAY, false))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterSpeech(AssistantCommandType.INFO_TIME, false))
    }

    @Test fun appLaunchesDoNotNeedSpeechCompletionGate() {
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterSpeech(AssistantCommandType.OPEN_YOUTUBE, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterSpeech(AssistantCommandType.OPEN_WHATSAPP, true))
    }
    @Test fun wakeHandoffFailureResumesOnlyWhenOptedInAndPermissionGranted() {
        assertTrue(KwsSpeechLifecycle.shouldResumeAfterHandoffFailure(true, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterHandoffFailure(false, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterHandoffFailure(true, false))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterHandoffFailure(false, false))
    }

    @Test fun ttsFailureResumesOnlyWhenRestartWasRequestedAndListenerStillEnabled() {
        assertTrue(KwsSpeechLifecycle.shouldResumeAfterTtsFailure(true, true, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterTtsFailure(false, true, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterTtsFailure(true, false, true))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterTtsFailure(true, true, false))
        assertFalse(KwsSpeechLifecycle.shouldResumeAfterTtsFailure(false, false, false))
    }

}
