package com.prempk.personalai

/** Pure permission/model gate for starting the user-visible microphone foreground service. */
object KwsStartGate {
    enum class Decision {
        MICROPHONE_PERMISSION_REQUIRED,
        MODEL_REQUIRED,
        NOTIFICATION_PERMISSION_REQUIRED,
        READY
    }

    fun evaluate(
        sdkInt: Int,
        microphonePermissionGranted: Boolean,
        modelInstalled: Boolean,
        notificationsPermissionGranted: Boolean
    ): Decision = when {
        !microphonePermissionGranted -> Decision.MICROPHONE_PERMISSION_REQUIRED
        !modelInstalled -> Decision.MODEL_REQUIRED
        sdkInt >= 33 && !notificationsPermissionGranted -> Decision.NOTIFICATION_PERMISSION_REQUIRED
        else -> Decision.READY
    }
}
