package com.example.kidtubelock.domain.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class StoredPinHash(
    val saltBase64: String,
    val hashBase64: String,
)

object PinHasher {
    private const val SALT_SIZE_BYTES = 16

    fun create(pin: String): StoredPinHash {
        val salt = ByteArray(SALT_SIZE_BYTES)
        SecureRandom().nextBytes(salt)
        return StoredPinHash(
            saltBase64 = salt.toBase64(),
            hashBase64 = hash(pin, salt).toBase64(),
        )
    }

    fun verify(
        pin: String,
        saltBase64: String,
        hashBase64: String,
    ): Boolean {
        if (saltBase64.isBlank() || hashBase64.isBlank()) {
            return false
        }

        val computedHash = hash(pin, Base64.getDecoder().decode(saltBase64))
        val storedHash = Base64.getDecoder().decode(hashBase64)
        return MessageDigest.isEqual(computedHash, storedHash)
    }

    private fun hash(
        pin: String,
        salt: ByteArray,
    ): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.toByteArray(Charsets.UTF_8))
        return digest.digest()
    }

    private fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)
}

