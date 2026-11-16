package com.prempk.personalai

/** Package-level allowlist for scroll gestures; an empty list means deny all apps. */
internal object AccessibilityAppAllowlist {
    fun isScrollAllowed(activePackage: String?, allowedPackages: Set<String>): Boolean =
        !activePackage.isNullOrBlank() && activePackage in allowedPackages
}
