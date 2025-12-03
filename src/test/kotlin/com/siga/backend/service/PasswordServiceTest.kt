package com.siga.backend.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PasswordServiceTest {

    private val passwordService = PasswordService()

    @Test
    fun `test hashPassword - creates different hash each time`() {
        val password = "testPassword123"
        val hash1 = passwordService.hashPassword(password)
        val hash2 = passwordService.hashPassword(password)

        assertNotEquals(hash1, hash2) // BCrypt genera salt aleatorio
        assertTrue(hash1.startsWith("\$2a\$")) // Formato BCrypt
        assertTrue(hash2.startsWith("\$2a\$"))
    }

    @Test
    fun `test verifyPassword - correct password returns true`() {
        val password = "testPassword123"
        val hash = passwordService.hashPassword(password)

        assertTrue(passwordService.verifyPassword(password, hash))
    }

    @Test
    fun `test verifyPassword - incorrect password returns false`() {
        val password = "testPassword123"
        val hash = passwordService.hashPassword(password)

        assertFalse(passwordService.verifyPassword("wrongPassword", hash))
    }

    @Test
    fun `test verifyPassword - empty password returns false`() {
        val password = "testPassword123"
        val hash = passwordService.hashPassword(password)

        assertFalse(passwordService.verifyPassword("", hash))
    }

    @Test
    fun `test hashPassword and verifyPassword - round trip`() {
        val passwords = listOf(
            "simple",
            "Complex@Password123!",
            "123456789",
            "a".repeat(100) // Password muy largo
        )

        passwords.forEach { password ->
            val hash = passwordService.hashPassword(password)
            assertTrue(passwordService.verifyPassword(password, hash), 
                "Password verification failed for: $password")
        }
    }
}

