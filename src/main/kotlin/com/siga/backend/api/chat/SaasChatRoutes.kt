package com.siga.backend.api.chat

import com.siga.backend.config.*
import com.siga.backend.services.OperationalAssistantService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas del asistente operativo (requiere autenticación)
 * Endpoint: POST /api/saas/chat
 */
fun Application.configureSaasChatRoutes() {
    routing {
        route("/api/saas") {
            authenticate("jwt") {
                post("/chat") {
                    try {
                        // Obtener información del usuario autenticado
                        val userId = call.getUserId()
                        val userRol = call.getUserRol()
                        
                        if (userId == null || userRol == null) {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ChatResponse(success = false, message = "Usuario no autenticado")
                            )
                            return@post
                        }
                        
                        // Verificar suscripción activa
                        if (!call.hasActiveSubscription()) {
                            call.respond(
                                HttpStatusCode.PaymentRequired,
                                ChatResponse(
                                    success = false,
                                    message = "Se requiere una suscripción activa para usar el asistente operativo. Por favor, suscríbete a un plan."
                                )
                            )
                            return@post
                        }
                        
                        val request = call.receive<ChatRequest>()
                        
                        if (request.message.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ChatResponse(success = false, message = "El mensaje no puede estar vacío")
                            )
                            return@post
                        }
                        
                        // Procesar mensaje con el asistente operativo
                        val result = OperationalAssistantService.processMessage(
                            userId = userId,
                            userRol = userRol,
                            userMessage = request.message
                        )
                        
                        result.fold(
                            onSuccess = { response ->
                                call.respond(
                                    HttpStatusCode.OK,
                                    ChatResponse(success = true, response = response)
                                )
                            },
                            onFailure = { error ->
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ChatResponse(
                                        success = false,
                                        message = "Error al procesar el mensaje: ${error.message}"
                                    )
                                )
                            }
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ChatResponse(success = false, message = "Error: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

