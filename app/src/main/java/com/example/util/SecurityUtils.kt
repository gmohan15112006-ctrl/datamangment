package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return bytesToHex(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val hashed = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashed)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}
