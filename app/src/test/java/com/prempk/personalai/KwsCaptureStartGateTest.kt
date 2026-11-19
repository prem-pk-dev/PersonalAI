package com.prempk.personalai

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KwsCaptureStartGateTest {
    @Test fun stopBeforeWorkerReachesStartGatePreventsCapture() {
        val gate = KwsCaptureStartGate()
        gate.reset()
        gate.stop { }

        var started = false
        assertFalse(gate.startIfAllowed { started = true })
        assertFalse(started)
    }

    @Test fun stopWaitsForAnInProgressStartAndBlocksEveryLaterStart() {
        val gate = KwsCaptureStartGate()
        gate.reset()
        val startEntered = CountDownLatch(1)
        val releaseStart = CountDownLatch(1)
        val started = AtomicBoolean(false)
        val stopped = AtomicBoolean(false)

        val starter = Thread {
            assertTrue(gate.startIfAllowed {
                startEntered.countDown()
                assertTrue(releaseStart.await(2, TimeUnit.SECONDS))
                started.set(true)
            })
        }
        starter.start()
        assertTrue("start callback did not enter", startEntered.await(2, TimeUnit.SECONDS))

        val stopper = Thread {
            gate.stop { stopped.set(true) }
        }
        stopper.start()
        releaseStart.countDown()

        starter.join(2_000)
        stopper.join(2_000)
        assertFalse("starter thread did not finish", starter.isAlive)
        assertFalse("stopper thread did not finish", stopper.isAlive)
        assertTrue(started.get())
        assertTrue(stopped.get())

        assertFalse(gate.startIfAllowed { throw AssertionError("capture started after Stop") })
    }
}
