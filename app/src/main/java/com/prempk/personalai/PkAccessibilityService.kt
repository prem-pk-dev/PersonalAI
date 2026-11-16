package com.prempk.personalai

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** Explicitly enabled, limited cross-app navigation. Does not inspect node text. */
class PkAccessibilityService : AccessibilityService() {
    enum class Action { SCROLL_BACKWARD, SCROLL_FORWARD, BACK, HOME }

    override fun onServiceConnected() { instance = this; super.onServiceConnected() }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }

    private fun execute(action: Action): Boolean = when (action) {
        Action.BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
        Action.HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
        Action.SCROLL_BACKWARD -> scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        Action.SCROLL_FORWARD -> scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    private fun scroll(action: Int): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            // Read only the active package identifier; never inspect screen text.
            val allowedPackages = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getStringSet(KEY_SCROLL_ALLOWLIST, emptySet()).orEmpty()
            if (!AccessibilityAppAllowlist.isScrollAllowed(root.packageName?.toString(), allowedPackages)) {
                false
            } else {
                findScrollable(root, action)
            }
        } finally {
            root.recycle()
        }
    }

    private fun findScrollable(node: AccessibilityNodeInfo, action: Int): Boolean {
        if (node.isScrollable && node.performAction(action)) return true
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            val handled = findScrollable(child, action)
            child.recycle()
            if (handled) return true
        }
        return false
    }

    companion object {
        private const val PREFS_NAME = "pk_voice_preferences"
        private const val KEY_SCROLL_ALLOWLIST = "accessibility_scroll_allowed_packages"
        @Volatile private var instance: PkAccessibilityService? = null
        fun perform(action: Action): Boolean = instance?.execute(action) ?: false
    }
}
