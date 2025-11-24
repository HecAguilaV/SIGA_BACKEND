package com.siga.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.siga.backend.models.*
import java.math.BigDecimal

class VentasTest {
    
    private suspend fun getAuthTokenWithSubscription(client: io.ktor.client.HttpClient, email: String): String? {
        // Registrar usuario
        runBlocking {
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "$email",
                        "password": "password123",
                        "nombre": "Test",
                        "rol": "ADMINISTRADOR"
                    }
                """.trimIndent())
            }
        }
        
        // Crear usuario comercial y suscripción
        transaction {
            // Usuario comercial
            val usuarioComercialId = UsuarioComercialTable.insert {
                it[UsuarioComercialTable.email] = email
                it[UsuarioComercialTable.passwordHash] = "hash"
                it[UsuarioComercialTable.nombre] = "Test"
                it[UsuarioComercialTable.activo] = true
            }
            
            val usuarioComercialIdValue = UsuarioComercialTable.select {
                UsuarioComercialTable.email eq email
            }.first()[UsuarioComercialTable.id]
            
            // Plan
            val planId = PlanTable.select { PlanTable.activo eq true }.firstOrNull()?.get(PlanTable.id)
                ?: run {
                    PlanTable.insert {
                        it[PlanTable.nombre] = "Plan Test"
                        it[PlanTable.precioMensual] = BigDecimal("10000")
                        it[PlanTable.limiteBodegas] = 1
                        it[PlanTable.limiteUsuarios] = 1
                        it[PlanTable.activo] = true
                    }
                    PlanTable.select { PlanTable.nombre eq "Plan Test" }.first()[PlanTable.id]
                }
            
            // Suscripción activa
            SuscripcionTable.insert {
                it[SuscripcionTable.usuarioId] = usuarioComercialIdValue
                it[SuscripcionTable.planId] = planId
                it[SuscripcionTable.fechaInicio] = java.time.LocalDate.now().minusDays(1)
                it[SuscripcionTable.fechaFin] = java.time.LocalDate.now().plusMonths(1)
                it[SuscripcionTable.estado] = "ACTIVA"
                it[SuscripcionTable.periodo] = "MENSUAL"
            }
        }
        
        // Login
        val loginResponse = runBlocking {
            client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "$email",
                        "password": "password123"
                    }
                """.trimIndent())
            }
        }
        
        val body = runBlocking { loginResponse.bodyAsText() }
        val regex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(body)?.groupValues?.get(1)
    }
    
    @Test
    fun testCreateVentaRequiresSubscription() = testApplication {
        application {
            testModule()
        }
        
        val email = "venta_nosub@test.com"
        val token = runBlocking { 
            // Solo registrar, sin suscripción
            runBlocking {
                client.post("/api/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                            "email": "$email",
                            "password": "password123",
                            "nombre": "Test",
                            "rol": "ADMINISTRADOR"
                        }
                    """.trimIndent())
                }
            }
            
            val loginResponse = runBlocking {
                client.post("/api/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                            "email": "$email",
                            "password": "password123"
                        }
                    """.trimIndent())
                }
            }
            
            val body = runBlocking { loginResponse.bodyAsText() }
            val regex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(body)?.groupValues?.get(1)
        }
        
        assertNotNull(token)
        
        val response = runBlocking {
            client.post("/api/saas/ventas") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody("""
                    {
                        "localId": 1,
                        "detalles": [
                            {
                                "productoId": 1,
                                "cantidad": 1,
                                "precioUnitario": "1000"
                            }
                        ]
                    }
                """.trimIndent())
            }
        }
        
        assertEquals(HttpStatusCode.PaymentRequired, response.status)
    }
    
    @Test
    fun testCreateVentaValidatesEmptyDetails() = testApplication {
        application {
            testModule()
        }
        
        val email = "venta_empty@test.com"
        val token = runBlocking { getAuthTokenWithSubscription(client, email) }
        assertNotNull(token)
        
        val response = runBlocking {
            client.post("/api/saas/ventas") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody("""
                    {
                        "localId": 1,
                        "detalles": []
                    }
                """.trimIndent())
            }
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = runBlocking { response.bodyAsText() }
        assertTrue(body.contains("al menos un producto"))
    }
    
    @Test
    fun testGetVentasRequiresSubscription() = testApplication {
        application {
            testModule()
        }
        
        val email = "venta_list@test.com"
        val token = runBlocking {
            runBlocking {
                client.post("/api/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                            "email": "$email",
                            "password": "password123",
                            "nombre": "Test",
                            "rol": "ADMINISTRADOR"
                        }
                    """.trimIndent())
                }
            }
            
            val loginResponse = runBlocking {
                client.post("/api/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                            "email": "$email",
                            "password": "password123"
                        }
                    """.trimIndent())
                }
            }
            
            val body = runBlocking { loginResponse.bodyAsText() }
            val regex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(body)?.groupValues?.get(1)
        }
        
        assertNotNull(token)
        
        val response = runBlocking {
            client.get("/api/saas/ventas") {
                header("Authorization", "Bearer $token")
            }
        }
        
        assertEquals(HttpStatusCode.PaymentRequired, response.status)
    }
    
    @Test
    fun testGetVentasWithSubscription() = testApplication {
        application {
            testModule()
        }
        
        val email = "venta_list_ok@test.com"
        val token = runBlocking { getAuthTokenWithSubscription(client, email) }
        assertNotNull(token)
        
        val response = runBlocking {
            client.get("/api/saas/ventas") {
                header("Authorization", "Bearer $token")
            }
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = runBlocking { response.bodyAsText() }
        assertTrue(body.contains("ventas"))
    }
}

