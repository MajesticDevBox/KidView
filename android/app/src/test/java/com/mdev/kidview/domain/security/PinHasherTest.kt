package com.mdev.kidview.domain.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test
    fun createAndVerify_matchesOriginalPin() {
        val stored = PinHasher.create("1234")

        assertTrue(
            PinHasher.verify(
                pin = "1234",
                saltBase64 = stored.saltBase64,
                hashBase64 = stored.hashBase64,
            ),
        )
    }

    @Test
    fun verify_rejectsWrongPin() {
        val stored = PinHasher.create("1234")

        assertFalse(
            PinHasher.verify(
                pin = "9876",
                saltBase64 = stored.saltBase64,
                hashBase64 = stored.hashBase64,
            ),
        )
    }

    @Test
    fun create_usesRandomSalt() {
        val first = PinHasher.create("1234")
        val second = PinHasher.create("1234")

        assertNotEquals(first.saltBase64, second.saltBase64)
        assertNotEquals(first.hashBase64, second.hashBase64)
    }
}

