package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FollowUpCommandResolverTest {
    @Test fun resolvesExplicitEnglishReopenReferences() {
        assertEquals("open YouTube", FollowUpCommandResolver.resolve("open it again", "YouTube"))
        assertEquals("open WhatsApp", FollowUpCommandResolver.resolve("Hey PK, reopen it!", "WhatsApp"))
        assertEquals("open Chrome", FollowUpCommandResolver.resolve("open the same app", "Chrome"))
    }

    @Test fun resolvesExplicitHindiAndHinglishReopenReferences() {
        assertEquals("open YouTube", FollowUpCommandResolver.resolve("उसे फिर से खोलो", "YouTube"))
        assertEquals("open Chrome", FollowUpCommandResolver.resolve("वही ऐप खोलो।", "Chrome"))
        assertEquals("open WhatsApp", FollowUpCommandResolver.resolve("same app kholo", "WhatsApp"))
    }

    @Test fun doesNotResolveWithoutSafeRecentAppContext() {
        assertNull(FollowUpCommandResolver.resolve("open it again", null))
        assertNull(FollowUpCommandResolver.resolve("open it again", " "))
        assertNull(FollowUpCommandResolver.resolve("open it again", "x".repeat(81)))
    }

    @Test fun doesNotInterpretArbitraryFollowUpTextAsAnAction() {
        assertNull(FollowUpCommandResolver.resolve("send it again", "WhatsApp"))
        assertNull(FollowUpCommandResolver.resolve("open my messages", "WhatsApp"))
        assertNull(FollowUpCommandResolver.resolve("delete it", "Chrome"))
    }
}
