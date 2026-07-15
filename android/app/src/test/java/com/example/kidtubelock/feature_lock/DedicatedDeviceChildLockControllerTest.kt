package com.example.kidtubelock.feature_lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DedicatedDeviceChildLockControllerTest {
    private val controller = DedicatedDeviceChildLockController()

    @Test
    fun launchSupport_isExplicitAboutPreviewState() {
        val support = controller.launchSupport()

        assertEquals(LockMode.DEDICATED_DEVICE, controller.lockMode)
        assertTrue(support.summary.contains("does not have privileged enforcement yet"))
        assertEquals("Dedicated-device preview checklist", support.screenPinningTitle)
        assertEquals(4, support.screenPinningSteps.size)
    }
}

