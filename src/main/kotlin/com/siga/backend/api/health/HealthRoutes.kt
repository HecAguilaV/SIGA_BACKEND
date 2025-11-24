package com.siga.backend.api.health

import com.siga.backend.config.DatabaseConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

@Serializable
data class HealthResponse(
    val status: String,
    val database: String,
    val timestamp: String
)

/**
 * Health check endpoint para Railway y monitoreo
 * 
 * Railway usa este endpoint para verificar que el servidor está funcionando
 */
fun Application.configureHealthRoutes() {
    routing {
        get("/health") {
            try {
                // Verificar conexión a base de datos
                val dbStatus = try {
                    transaction {
                        exec("SELECT 1")
                    }
                    "connected"
                } catch (e: Exception) {
                    "disconnected"
                }
                
                val status = if (dbStatus == "connected") "healthy" else "unhealthy"
                
                call.respond(
                    HttpStatusCode.OK,
                    HealthResponse(
                        status = status,
                        database = dbStatus,
                        timestamp = java.time.Instant.now().toString()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    HealthResponse(
                        status = "unhealthy",
                        database = "error",
                        timestamp = java.time.Instant.now().toString()
                    )
                )
            }
        }
    }
}

