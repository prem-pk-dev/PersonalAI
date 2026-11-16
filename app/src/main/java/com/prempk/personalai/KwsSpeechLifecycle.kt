package com.prempk.personalai

/**
 * Keeps the PK microphone listener stopped until each command's spoken response completes.
 */
internal object KwsSpeechLifecycle {
    fun shouldResumeAfterHandoffFailure(listenerEnabled: Boolean, microphonePermissionGranted: Boolean): Boolean =
        listenerEnabled && microphonePermissionGranted

    fun shouldResumeSpeechRecognitionAfterPrompt(wakeMode: Boolean, awaitingCommand: Boolean): Boolean =
        wakeMode && awaitingCommand

    fun shouldResumeAfterTtsFailure(
        restartRequested: Boolean,
        listenerEnabled: Boolean,
        microphonePermissionGranted: Boolean
    ): Boolean = restartRequested && listenerEnabled && microphonePermissionGranted

    /**
     * Every dispatched command currently produces a spoken confirmation or error.
     * Keep the microphone stopped until that utterance completes, regardless of command type.
     */
    @Suppress("UNUSED_PARAMETER")
    fun shouldResumeAfterSpeech(commandType: AssistantCommandType, listenerEnabled: Boolean): Boolean =
        listenerEnabled
}
