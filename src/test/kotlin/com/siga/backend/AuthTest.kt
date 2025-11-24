package com.siga.backend

import com.siga.backend.api.auth.AuthResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class AuthTest {
    
    @Test
    fun testRegister() = testApplication {
        application {
            testModule()
        }
        
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@example.com",
                    "password": "password123",
                    "nombre": "Test",
                    "apellido": "User",
                    "rol": "OPERADOR"
                }
            """.trimIndent())
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("success"))
    }
    
    @Test
    fun testLogin() = testApplication {
        application {
            testModule()
        }
        
        // Primero registrar un usuario
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "login@example.com",
                    "password": "password123",
                    "nombre": "Login",
                    "rol": "OPERADOR"
                }
            """.trimIndent())
        }
        
        // Luego hacer login
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "login@example.com",
                    "password": "password123"
                }
            """.trimIndent())
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("accessToken"))
        assertTrue(body.contains("refreshToken"))
    }
    
    @Test
    fun testLoginInvalidCredentials() = testApplication {
        application {
            testModule()
        }
        
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "nonexistent@example.com",
                    "password": "wrongpassword"
                }
            """.trimIndent())
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Credenciales inválidas"))
    }
    
    @Test
    fun testRefreshToken() = testApplication {
        application {
            testModule()
        }
        
        // Registrar y hacer login para obtener tokens
        val registerResponse = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "refresh@example.com",
                    "password": "password123",
                    "nombre": "Refresh",
                    "rol": "OPERADOR"
                }
            """.trimIndent())
        }
        
        val registerBody = registerResponse.bodyAsText()
        val refreshToken = extractRefreshToken(registerBody)
        
        if (refreshToken != null) {
            val response = client.post("/api/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "refreshToken": "$refreshToken"
                    }
                """.trimIndent())
            }
            
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("accessToken"))
        }
    }
    
    private fun extractRefreshToken(json: String): String? {
        val regex = """"refreshToken"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }
}

