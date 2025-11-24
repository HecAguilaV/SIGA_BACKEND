package com.siga.backend.api

import com.siga.backend.config.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * EJEMPLO: Cómo usar el middleware de autenticación
 * 
 * Este archivo muestra diferentes formas de proteger rutas.
 * Puedes eliminar este archivo cuando implementes tus rutas reales.
 */

@Serializable
data class ProtectedResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?,
    val email: String?,
    val rol: String?
)

fun Application.configureExampleProtectedRoutes() {
    routing {
        // ============================================
        // EJEMPLO 1: Ruta protegida (cualquier usuario autenticado)
        // ============================================
        authenticate("jwt") {
            get("/api/protected") {
                val userId = call.getUserId()
                val email = call.getUserEmail()
                val rol = call.getUserRol()
                
                call.respond(
                    HttpStatusCode.OK,
                    ProtectedResponse(
                        success = true,
                        message = "¡Acceso autorizado!",
                        userId = userId,
                        email = email,
                        rol = rol
                    )
                )
            }
        }
        
        // ============================================
        // EJEMPLO 2: Ruta solo para ADMINISTRADOR
        // ============================================
        authenticate("jwt") {
            get("/api/admin-only") {
                if (!call.isAdmin()) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ProtectedResponse(
                            success = false,
                            message = "Solo administradores pueden acceder",
                            userId = null,
                            email = null,
                            rol = null
                        )
                    )
                    return@get
                }
                
                call.respond(
                    HttpStatusCode.OK,
                    ProtectedResponse(
                        success = true,
                        message = "Acceso de administrador concedido",
                        userId = call.getUserId(),
                        email = call.getUserEmail(),
                        rol = call.getUserRol()
                    )
                )
            }
        }
        
        // ============================================
        // EJEMPLO 3: Ruta para múltiples roles
        // ============================================
        authenticate("jwt") {
            get("/api/operadores-y-admin") {
                if (!call.hasAnyRole("ADMINISTRADOR", "OPERADOR")) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ProtectedResponse(
                            success = false,
                            message = "Solo administradores y operadores pueden acceder",
                            userId = null,
                            email = null,
                            rol = null
                        )
                    )
                    return@get
                }
                
                call.respond(
                    HttpStatusCode.OK,
                    ProtectedResponse(
                        success = true,
                        message = "Acceso concedido",
                        userId = call.getUserId(),
                        email = call.getUserEmail(),
                        rol = call.getUserRol()
                    )
                )
            }
        }
        
        // ============================================
        // EJEMPLO 4: Ruta pública (sin autenticación)
        // ============================================
        get("/api/public") {
            call.respond(
                HttpStatusCode.OK,
                ProtectedResponse(
                    success = true,
                    message = "Esta ruta es pública, no requiere autenticación",
                    userId = null,
                    email = null,
                    rol = null
                )
            )
        }
    }
}

/**
 * CÓMO USAR EN TUS RUTAS REALES:
 * 
 * 1. Para proteger una ruta, envuélvela en authenticate("jwt"):
 * 
 *    authenticate("jwt") {
 *        get("/api/productos") {
 *            val userId = call.getUserId() // Obtener ID del usuario
 *            // Tu lógica aquí
 *        }
 *    }
 * 
 * 2. Para verificar roles:
 * 
 *    authenticate("jwt") {
 *        post("/api/productos") {
 *            if (!call.isAdmin()) {
 *                call.respond(HttpStatusCode.Forbidden, ...)
 *                return@post
 *            }
 *            // Solo admin puede crear productos
 *        }
 *    }
 * 
 * 3. Para múltiples roles:
 * 
 *    authenticate("jwt") {
 *        get("/api/ventas") {
 *            if (!call.hasAnyRole("ADMINISTRADOR", "OPERADOR")) {
 *                call.respond(HttpStatusCode.Forbidden, ...)
 *                return@get
 *            }
 *            // Admin y Operador pueden ver ventas
 *        }
 *    }
 */

