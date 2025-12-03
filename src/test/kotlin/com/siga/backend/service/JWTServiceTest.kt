package com.siga.backend.service

import com.auth0.jwt.interfaces.DecodedJWT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class JWTServiceTest {

    private lateinit var jwtService: JWTService

    @BeforeEach
    fun setUp() {
        jwtService = JWTService(
            secret = "test_secret_key_for_jwt_testing_purposes_only",
            issuer = "test-issuer",
            accessTokenExpiry = 86400000L, // 1 día
            refreshTokenExpiry = 604800000L // 7 días
        )
    }

    @Test
    fun `test generateAccessToken - creates valid token`() {
        val token = jwtService.generateAccessToken(
            userId = 1,
            email = "test@example.com",
            rol = "ADMINISTRADOR"
        )

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        val decoded = jwtService.verifyToken(token)
        assertNotNull(decoded)
        assertEquals("1", decoded!!.subject)
        assertEquals("test@example.com", decoded.getClaim("email").asString())
        assertEquals("ADMINISTRADOR", decoded.getClaim("rol").asString())
        assertEquals("access", decoded.getClaim("type").asString())
        assertEquals("test-issuer", decoded.issuer)
    }

    @Test
    fun `test generateRefreshToken - creates valid token`() {
        val token = jwtService.generateRefreshToken(userId = 1)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        val decoded = jwtService.verifyToken(token)
        assertNotNull(decoded)
        assertEquals("1", decoded!!.subject)
        assertEquals("refresh", decoded.getClaim("type").asString())
        assertEquals("test-issuer", decoded.issuer)
    }

    @Test
    fun `test verifyToken - valid token returns decoded JWT`() {
        val token = jwtService.generateAccessToken(1, "test@example.com", "OPERADOR")
        val decoded = jwtService.verifyToken(token)

        assertNotNull(decoded)
        assertEquals("1", decoded!!.subject)
        assertEquals("test@example.com", decoded.getClaim("email").asString())
    }

    @Test
    fun `test verifyToken - invalid token returns null`() {
        val decoded = jwtService.verifyToken("invalid.token.here")
        assertNull(decoded)
    }

    @Test
    fun `test verifyToken - expired token returns null`() {
        // Crear un servicio con expiración muy corta
        val shortLivedService = JWTService(
            secret = "test_secret",
            issuer = "test-issuer",
            accessTokenExpiry = 1L, // 1ms
            refreshTokenExpiry = 1L
        )

        val token = shortLivedService.generateAccessToken(1, "test@example.com")
        
        // Esperar un poco para que expire
        Thread.sleep(10)
        
        val decoded = shortLivedService.verifyToken(token)
        assertNull(decoded)
    }

    @Test
    fun `test generateAccessToken - without rol`() {
        val token = jwtService.generateAccessToken(
            userId = 2,
            email = "user@example.com",
            rol = null
        )

        val decoded = jwtService.verifyToken(token)
        assertNotNull(decoded)
        assertNull(decoded!!.getClaim("rol").asString())
    }
}

