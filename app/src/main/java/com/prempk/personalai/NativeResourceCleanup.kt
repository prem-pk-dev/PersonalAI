package com.prempk.personalai

/**
 * Runs native-resource cleanup actions independently.
 *
 * JNI-backed release calls can throw; one failure must not prevent later resources
 * (or the caller's subsequent WakeLock cleanup) from being released.
 */
internal object NativeResourceCleanup {
    fun releaseAll(vararg actions: () -> Unit) {
        actions.forEach { action -> runCatching { action() } }
    }
}
