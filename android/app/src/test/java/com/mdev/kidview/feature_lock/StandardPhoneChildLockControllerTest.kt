package com.mdev.kidview.feature_lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StandardPhoneChildLockControllerTest {
    private val controller = StandardPhoneChildLockController()

    @Test
    fun launchSupport_containsScreenPinningChecklist() {
        val support = controller.launchSupport()

        assertTrue(support.summary.isNotBlank())
        assertEquals("Before handing over the phone", support.screenPinningTitle)
        assertEquals(4, support.screenPinningSteps.size)
    }

    @Test
    fun sessionPolicy_hardensStandardPhoneMode() {
        val policy = controller.sessionPolicy()

        assertTrue(policy.keepScreenAwake)
        assertTrue(policy.useImmersiveMode)
        assertTrue(policy.absorbBackPress)
        assertFalse(controller.launchSupport().inSessionHint.isBlank())
    }
}
