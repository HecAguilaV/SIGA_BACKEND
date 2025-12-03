package com.siga.backend.api.health

import com.siga.backend.config.DatabaseConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.withTimeout
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
 * Responde rápidamente para evitar timeouts en Railway
 */
fun Application.configureHealthRoutes() {
    routing {
        get("/health") {
            try {
                // Verificar conexión a base de datos (con timeout corto)
                val dbStatus = try {
                    // Usar un timeout corto para no bloquear el health check
                    kotlinx.coroutines.withTimeout(2000) {
                        transaction {
                            exec("SELECT 1")
                        }
                    }
                    "connected"
                } catch (e: Exception) {
                    // Si falla la DB, aún respondemos OK para que Railway no falle el deploy
                    // pero indicamos el estado de la DB
                    "disconnected"
                }
                
                // Siempre respondemos OK si el servidor está corriendo
                // Railway solo necesita saber que el servidor responde
                val status = "healthy"
                
                call.respond(
                    HttpStatusCode.OK,
                    HealthResponse(
                        status = status,
                        database = dbStatus,
                        timestamp = java.time.Instant.now().toString()
                    )
                )
            } catch (e: Exception) {
                // Solo respondemos error si hay un problema crítico del servidor
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

