package com.prempk.personalai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityAppAllowlistTest {
    @Test fun emptyAllowlistDeniesEveryApp() {
        assertFalse(AccessibilityAppAllowlist.isScrollAllowed("com.google.android.youtube", emptySet()))
    }

    @Test fun onlyExplicitlyAllowedPackagesCanScroll() {
        val allowed = setOf("com.google.android.youtube", "com.whatsapp")
        assertTrue(AccessibilityAppAllowlist.isScrollAllowed("com.google.android.youtube", allowed))
        assertTrue(AccessibilityAppAllowlist.isScrollAllowed("com.whatsapp", allowed))
        assertFalse(AccessibilityAppAllowlist.isScrollAllowed("com.android.chrome", allowed))
    }

    @Test fun missingActivePackageIsDenied() {
        val allowed = setOf("com.google.android.youtube")
        assertFalse(AccessibilityAppAllowlist.isScrollAllowed(null, allowed))
        assertFalse(AccessibilityAppAllowlist.isScrollAllowed("", allowed))
    }
}
