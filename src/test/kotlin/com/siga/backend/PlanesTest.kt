package com.siga.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class PlanesTest {
    
    @Test
    fun testGetPlanesPublic() = testApplication {
        application {
            testModule()
        }
        
        // Los planes son públicos, no requieren autenticación
        val response = client.get("/api/comercial/planes")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("planes"))
    }
    
    @Test
    fun testGetPlanById() = testApplication {
        application {
            testModule()
        }
        
        // Intentar obtener plan con ID 1
        val response = client.get("/api/comercial/planes/1")
        
        // Puede ser OK si existe, o NotFound si no hay datos
        assertTrue(
            response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound,
            "Debe retornar OK o NotFound"
        )
    }
}

