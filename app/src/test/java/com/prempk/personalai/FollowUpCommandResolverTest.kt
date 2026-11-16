package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FollowUpCommandResolverTest {
    @Test fun resolvesExplicitEnglishReopenReferencesToExactPackage() {
        assertEquals("com.google.android.youtube",
            FollowUpCommandResolver.resolvePackage("open it again", "com.google.android.youtube"))
        assertEquals("com.whatsapp.w4b",
            FollowUpCommandResolver.resolvePackage("Hey PK, reopen it!", "com.whatsapp.w4b"))
        assertEquals("com.android.chrome",
            FollowUpCommandResolver.resolvePackage("open the same app", "com.android.chrome"))
    }

    @Test fun resolvesExplicitHindiAndHinglishReopenReferencesToExactPackage() {
        assertEquals("com.google.android.youtube",
            FollowUpCommandResolver.resolvePackage("उसे फिर से खोलो", "com.google.android.youtube"))
        assertEquals("com.android.chrome",
            FollowUpCommandResolver.resolvePackage("वही ऐप खोलो।", "com.android.chrome"))
        assertEquals("com.whatsapp",
            FollowUpCommandResolver.resolvePackage("same app kholo", "com.whatsapp"))
    }

    @Test fun doesNotResolveWithoutSafeRecentPackageContext() {
        assertNull(FollowUpCommandResolver.resolvePackage("open it again", null))
        assertNull(FollowUpCommandResolver.resolvePackage("open it again", " "))
        assertNull(FollowUpCommandResolver.resolvePackage("open it again", "WhatsApp"))
        assertNull(FollowUpCommandResolver.resolvePackage("open it again", "x".repeat(256)))
    }

    @Test fun doesNotInterpretArbitraryFollowUpTextAsAnAction() {
        assertNull(FollowUpCommandResolver.resolvePackage("send it again", "com.whatsapp"))
        assertNull(FollowUpCommandResolver.resolvePackage("open my messages", "com.whatsapp"))
        assertNull(FollowUpCommandResolver.resolvePackage("delete it", "com.android.chrome"))
    }
}
