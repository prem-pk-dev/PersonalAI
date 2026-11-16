package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Test

class NativeResourceCleanupTest {
    @Test
    fun failureInOneReleaseDoesNotSkipLaterResources() {
        val released = mutableListOf<String>()

        NativeResourceCleanup.releaseAll(
            { released += "recorder-stop"; error("native stop failed") },
            { released += "recorder-release" },
            { released += "stream-release" },
            { released += "spotter-release" }
        )

        assertEquals(
            listOf("recorder-stop", "recorder-release", "stream-release", "spotter-release"),
            released
        )
    }

    @Test
    fun allSuccessfulReleaseActionsRunInOrder() {
        val released = mutableListOf<Int>()

        NativeResourceCleanup.releaseAll(
            { released += 1 },
            { released += 2 },
            { released += 3 }
        )

        assertEquals(listOf(1, 2, 3), released)
    }

    @Test
    fun emptyCleanupListIsSafe() {
        NativeResourceCleanup.releaseAll()
    }
}
