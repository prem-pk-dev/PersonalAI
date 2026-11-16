package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class KwsStartGateTest {
    @Test fun microphonePermissionIsRequiredBeforeAnyOtherStartupStep() {
        assertEquals(
            KwsStartGate.Decision.MICROPHONE_PERMISSION_REQUIRED,
            KwsStartGate.evaluate(31, false, false, false)
        )
    }

    @Test fun installedWakeWordModelIsRequiredAfterMicrophonePermission() {
        assertEquals(
            KwsStartGate.Decision.MODEL_REQUIRED,
            KwsStartGate.evaluate(31, true, false, false)
        )
    }

    @Test fun androidTwelveDoesNotRequireRuntimeNotificationPermission() {
        assertEquals(
            KwsStartGate.Decision.READY,
            KwsStartGate.evaluate(31, true, true, false)
        )
    }

    @Test fun androidThirteenAndLaterRequireNotificationPermission() {
        listOf(33, 34, 35).forEach { sdk ->
            assertEquals(
                "SDK $sdk",
                KwsStartGate.Decision.NOTIFICATION_PERMISSION_REQUIRED,
                KwsStartGate.evaluate(sdk, true, true, false)
            )
            assertEquals(
                "SDK $sdk after grant",
                KwsStartGate.Decision.READY,
                KwsStartGate.evaluate(sdk, true, true, true)
            )
        }
    }

    @Test fun readyStateRequiresAllApplicablePrerequisites() {
        assertEquals(
            KwsStartGate.Decision.READY,
            KwsStartGate.evaluate(31, true, true, true)
        )
    }
}
