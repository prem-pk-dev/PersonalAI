package com.prempk.personalai

/**
 * Fixed-capacity PCM16 capture buffer.
 *
 * Uses one primitive ShortArray instead of boxing each microphone sample, and never
 * grows beyond the requested duration. Calls are made by the single capture worker.
 */
internal class PcmCaptureBuffer(capacity: Int) {
    private val samples: ShortArray
    var size: Int = 0
        private set

    init {
        require(capacity > 0) { "PCM capture capacity must be positive." }
        samples = ShortArray(capacity)
    }

    val isFull: Boolean
        get() = size == samples.size

    fun append(frame: ShortArray, length: Int = frame.size): Int {
        require(length in 0..frame.size) { "PCM frame length is invalid." }
        val copied = minOf(length, samples.size - size)
        if (copied > 0) {
            frame.copyInto(samples, destinationOffset = size, startIndex = 0, endIndex = copied)
            size += copied
        }
        return copied
    }

    fun toShortArray(): ShortArray {
        require(size > 0) { "No PCM samples have been captured." }
        return samples.copyOf(size)
    }
}
