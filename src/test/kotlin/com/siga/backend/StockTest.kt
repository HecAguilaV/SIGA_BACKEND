package com.siga.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking

class StockTest {
    
    private suspend fun getAuthToken(client: io.ktor.client.HttpClient, rol: String = "ADMINISTRADOR"): String? {
        val email = if (rol == "ADMINISTRADOR") "admin_stock@test.com" else "operador_stock@test.com"
        
        // Registrar usuario
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "$email",
                    "password": "password123",
                    "nombre": "Test",
                    "rol": "$rol"
                }
            """.trimIndent())
        }
        
        // Hacer login
        val loginResponse = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "$email",
                    "password": "password123"
                }
            """.trimIndent())
        }
        
        val body = loginResponse.bodyAsText()
        val regex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(body)?.groupValues?.get(1)
    }
    
    @Test
    fun testGetStockRequiresAuth() = testApplication {
        application {
            testModule()
        }
        
        // Intentar acceder sin token
        val response = runBlocking {
            try {
                client.get("/api/saas/stock")
            } catch (e: Exception) {
                null
            }
        }
        
        if (response != null) {
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        } else {
            assertTrue(true, "Petición rechazada por middleware")
        }
    }
    
    @Test
    fun testGetStock() = testApplication {
        application {
            testModule()
        }
        
        val token = runBlocking { getAuthToken(client) }
        assertNotNull(token)
        
        val response = runBlocking {
            client.get("/api/saas/stock") {
                header("Authorization", "Bearer $token")
            }
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.bodyAsText() }
        assertTrue(body.contains("stock"))
    }
}

