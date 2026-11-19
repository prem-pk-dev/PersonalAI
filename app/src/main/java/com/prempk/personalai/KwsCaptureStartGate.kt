package com.prempk.personalai

/**
 * Serializes the final microphone-start operation with Stop.
 * The callback passed to startIfAllowed runs while the gate is held.
 */
internal class KwsCaptureStartGate {
    private val lock = Any()
    private var stopped = false

    fun reset() {
        synchronized(lock) { stopped = false }
    }

    fun startIfAllowed(start: () -> Unit): Boolean = synchronized(lock) {
        if (stopped) {
            false
        } else {
            start()
            true
        }
    }

    fun stop(stopCapture: () -> Unit) {
        synchronized(lock) {
            stopped = true
            stopCapture()
        }
    }
}
