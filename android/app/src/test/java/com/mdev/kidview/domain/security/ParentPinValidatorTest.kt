package com.mdev.kidview.domain.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParentPinValidatorTest {
    @Test
    fun sanitize_keeps_only_digits_and_limits_length() {
        val sanitized = ParentPinValidator.sanitize("12ab34-56789")

        assertEquals("12345678", sanitized)
    }

    @Test
    fun validateNewPin_rejects_short_pin() {
        val error = ParentPinValidator.validateNewPin(
            pin = "123",
            confirmPin = "123",
        )

        assertEquals("Use at least 4 digits for the parent PIN.", error)
    }

    @Test
    fun validatePinChange_requires_current_pin() {
        val error = ParentPinValidator.validatePinChange(
            currentPin = "",
            newPin = "1234",
            confirmNewPin = "1234",
        )

        assertEquals("Enter the current parent PIN.", error)
    }

    @Test
    fun validatePinChange_rejects_same_pin() {
        val error = ParentPinValidator.validatePinChange(
            currentPin = "1234",
            newPin = "1234",
            confirmNewPin = "1234",
        )

        assertEquals("Choose a new PIN that is different from the current PIN.", error)
    }

    @Test
    fun validatePinChange_accepts_valid_update() {
        val error = ParentPinValidator.validatePinChange(
            currentPin = "1234",
            newPin = "5678",
            confirmNewPin = "5678",
        )

        assertNull(error)
    }
}
