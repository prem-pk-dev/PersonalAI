package com.prempk.personalai

/**
 * Serializes microphone session ownership and the final microphone-start operation.
 * A worker may start capture only while its generation is still current.
 */
internal class KwsCaptureStartGate {
    private val lock = Any()
    private var stopped = true
    private var generation = 0L

    fun reset(): Long = synchronized(lock) {
        stopped = false
        ++generation
    }

    fun isCurrent(session: Long): Boolean = synchronized(lock) {
        !stopped && generation == session
    }

    fun startIfAllowed(session: Long, start: () -> Unit): Boolean = synchronized(lock) {
        if (stopped || generation != session) {
            false
        } else {
            start()
            true
        }
    }

    fun stop(stopCapture: () -> Unit) {
        synchronized(lock) {
            stopped = true
            ++generation
            stopCapture()
        }
    }

    fun withLock(action: () -> Unit) {
        synchronized(lock) { action() }
    }
}
