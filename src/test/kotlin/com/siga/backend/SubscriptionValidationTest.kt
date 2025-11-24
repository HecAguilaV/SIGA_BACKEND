package com.siga.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.siga.backend.models.UsuarioComercialTable
import com.siga.backend.models.SuscripcionTable
import com.siga.backend.models.PlanTable
import java.time.LocalDate

class SubscriptionValidationTest {
    
    private suspend fun getAuthToken(client: io.ktor.client.HttpClient, email: String, rol: String = "ADMINISTRADOR"): String? {
        // Registrar usuario
        runBlocking {
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
        }
        
        // Hacer login
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
    fun testProductosRequiresActiveSubscription() = testApplication {
        application {
            testModule()
        }
        
        // Crear usuario sin suscripción
        val email = "nosub@test.com"
        val token = runBlocking { getAuthToken(client, email) }
        assertNotNull(token, "Debe obtener un token de autenticación")
        
        // Intentar acceder a productos sin suscripción
        val response = runBlocking {
            client.get("/api/saas/productos") {
                header("Authorization", "Bearer $token")
            }
        }
        
        // Debe retornar 402 Payment Required
        assertEquals(HttpStatusCode.PaymentRequired, response.status)
        val body = runBlocking { response.bodyAsText() }
        assertTrue(body.contains("suscripción activa") || body.contains("subscription"))
    }
    
    @Test
    fun testProductosWithActiveSubscription() = testApplication {
        application {
            testModule()
        }
        
        // Crear usuario con suscripción activa
        val email = "withsub@test.com"
        val token = runBlocking { getAuthToken(client, email) }
        assertNotNull(token)
        
        // Crear usuario comercial y suscripción en la BD
        transaction {
            // Crear usuario comercial
            UsuarioComercialTable.insert {
                it[UsuarioComercialTable.email] = email
                it[UsuarioComercialTable.passwordHash] = "hash"
                it[UsuarioComercialTable.nombre] = "Test"
                it[UsuarioComercialTable.activo] = true
            }
            
            // Obtener ID del usuario comercial
            val usuarioComercialId = UsuarioComercialTable.select {
                UsuarioComercialTable.email eq email
            }.first()[UsuarioComercialTable.id]
            
            // Obtener o crear un plan
            val planId = PlanTable.select { PlanTable.activo eq true }.firstOrNull()?.get(PlanTable.id)
                ?: run {
                    PlanTable.insert {
                        it[PlanTable.nombre] = "Plan Test"
                        it[PlanTable.precioMensual] = java.math.BigDecimal("10000")
                        it[PlanTable.limiteBodegas] = 1
                        it[PlanTable.limiteUsuarios] = 1
                        it[PlanTable.activo] = true
                    }
                    PlanTable.select { PlanTable.nombre eq "Plan Test" }.first()[PlanTable.id]
                }
            
            // Crear suscripción activa
            SuscripcionTable.insert {
                it[SuscripcionTable.usuarioId] = usuarioComercialId
                it[SuscripcionTable.planId] = planId
                it[SuscripcionTable.fechaInicio] = LocalDate.now().minusDays(1)
                it[SuscripcionTable.fechaFin] = LocalDate.now().plusMonths(1)
                it[SuscripcionTable.estado] = "ACTIVA"
                it[SuscripcionTable.periodo] = "MENSUAL"
            }
        }
        
        // Intentar acceder a productos con suscripción activa
        val response = runBlocking {
            client.get("/api/saas/productos") {
                header("Authorization", "Bearer $token")
            }
        }
        
        // Debe retornar 200 OK
        assertEquals(HttpStatusCode.OK, response.status)
    }
    
    @Test
    fun testStockRequiresActiveSubscription() = testApplication {
        application {
            testModule()
        }
        
        val email = "nostock@test.com"
        val token = runBlocking { getAuthToken(client, email) }
        assertNotNull(token)
        
        val response = runBlocking {
            client.get("/api/saas/stock") {
                header("Authorization", "Bearer $token")
            }
        }
        
        assertEquals(HttpStatusCode.PaymentRequired, response.status)
    }
    
    @Test
    fun testChatRequiresActiveSubscription() = testApplication {
        application {
            testModule()
        }
        
        val email = "nochat@test.com"
        val token = runBlocking { getAuthToken(client, email) }
        assertNotNull(token)
        
        val response = runBlocking {
            client.post("/api/saas/chat") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody("""
                    {
                        "message": "Hola"
                    }
                """.trimIndent())
            }
        }
        
        assertEquals(HttpStatusCode.PaymentRequired, response.status)
    }
}

