package com.prempk.personalai

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmCaptureBufferTest {
    @Test fun appendsFramesInOrderAndReturnsIndependentSnapshot() {
        val buffer = PcmCaptureBuffer(5)
        assertEquals(3, buffer.append(shortArrayOf(1, 2, 3)))
        assertEquals(2, buffer.append(shortArrayOf(4, 5, 6), 2))
        assertTrue(buffer.isFull)
        val snapshot = buffer.toShortArray()
        assertArrayEquals(shortArrayOf(1, 2, 3, 4, 5), snapshot)
        snapshot[0] = 99
        assertArrayEquals(shortArrayOf(1, 2, 3, 4, 5), buffer.toShortArray())
    }

    @Test fun truncatesAtCapacityWithoutGrowing() {
        val buffer = PcmCaptureBuffer(3)
        assertEquals(3, buffer.append(shortArrayOf(1, 2, 3, 4, 5)))
        assertEquals(0, buffer.append(shortArrayOf(6, 7)))
        assertEquals(3, buffer.size)
        assertTrue(buffer.isFull)
        assertArrayEquals(shortArrayOf(1, 2, 3), buffer.toShortArray())
    }

    @Test fun respectsPartialFrameLength() {
        val buffer = PcmCaptureBuffer(4)
        assertEquals(2, buffer.append(shortArrayOf(7, 8, 9), 2))
        assertFalse(buffer.isFull)
        assertArrayEquals(shortArrayOf(7, 8), buffer.toShortArray())
    }

    @Test fun rejectsInvalidCapacityAndFrameLength() {
        assertThrows(IllegalArgumentException::class.java) { PcmCaptureBuffer(0) }
        val buffer = PcmCaptureBuffer(2)
        assertThrows(IllegalArgumentException::class.java) {
            buffer.append(shortArrayOf(1), 2)
        }
    }

    @Test fun rejectsSnapshotWhenNoSamplesWereCaptured() {
        val buffer = PcmCaptureBuffer(2)
        assertThrows(IllegalArgumentException::class.java) { buffer.toShortArray() }
    }
}
