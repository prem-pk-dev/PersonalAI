package com.prempk.personalai

/**
 * Keeps the PK microphone listener stopped while the assistant speaks.
 * Only commands with an audible response need this speech-completion handoff.
 */
internal object KwsSpeechLifecycle {
    fun shouldResumeAfterHandoffFailure(listenerEnabled: Boolean, microphonePermissionGranted: Boolean): Boolean =
        listenerEnabled && microphonePermissionGranted

    fun shouldResumeAfterTtsFailure(
        restartRequested: Boolean,
        listenerEnabled: Boolean,
        microphonePermissionGranted: Boolean
    ): Boolean = restartRequested && listenerEnabled && microphonePermissionGranted

    fun shouldResumeAfterSpeech(commandType: AssistantCommandType, listenerEnabled: Boolean): Boolean {
        if (!listenerEnabled) return false
        return commandType in setOf(
            AssistantCommandType.INFO_TIME,
            AssistantCommandType.INFO_DATE,
            AssistantCommandType.GREETING,
            AssistantCommandType.INFO_BATTERY,
            AssistantCommandType.HELP,
            AssistantCommandType.UNKNOWN,
            AssistantCommandType.SCROLL_UP,
            AssistantCommandType.SCROLL_DOWN,
            AssistantCommandType.GO_BACK,
            AssistantCommandType.GO_HOME,
            AssistantCommandType.MEDIA_PLAY,
            AssistantCommandType.MEDIA_PAUSE,
            AssistantCommandType.MEDIA_NEXT,
            AssistantCommandType.MEDIA_PREVIOUS,
            AssistantCommandType.VOLUME_UP,
            AssistantCommandType.VOLUME_DOWN,
            AssistantCommandType.VOLUME_MUTE
        )
    }
}
