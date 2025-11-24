package com.siga.backend.api.chat

import com.siga.backend.services.CommercialAssistantService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas del asistente comercial (público)
 * Endpoint: POST /api/comercial/chat
 */
fun Application.configureCommercialChatRoutes() {
    routing {
        route("/api/comercial") {
            post("/chat") {
                try {
                    val request = call.receive<ChatRequest>()
                    
                    if (request.message.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ChatResponse(success = false, message = "El mensaje no puede estar vacío")
                        )
                        return@post
                    }
                    
                    // Procesar mensaje con el asistente comercial
                    val result = CommercialAssistantService.processMessage(request.message)
                    
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

