package com.example.kidtubelock.domain.security

object ParentPinValidator {
    private const val minimumPinLength = 4
    private const val maximumPinLength = 8

    fun sanitize(value: String): String = value.filter(Char::isDigit).take(maximumPinLength)

    fun validateNewPin(
        pin: String,
        confirmPin: String,
    ): String? = when {
        pin.length < minimumPinLength -> "Use at least 4 digits for the parent PIN."
        pin != confirmPin -> "PIN entries do not match."
        else -> null
    }

    fun validatePinChange(
        currentPin: String,
        newPin: String,
        confirmNewPin: String,
    ): String? = when {
        currentPin.isBlank() -> "Enter the current parent PIN."
        currentPin == newPin -> "Choose a new PIN that is different from the current PIN."
        else -> validateNewPin(newPin, confirmNewPin)
    }
}
