package com.siga.backend.controller

import com.siga.backend.service.CommercialAssistantService
import com.siga.backend.service.OperationalAssistantService
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ChatRequest(
    @field:NotBlank val message: String
)

data class ChatResponse(
    val success: Boolean,
    val response: String? = null,
    val message: String? = null,
    val action: ActionInfo? = null  // Información sobre acción ejecutada
)

data class ActionInfo(
    val executed: Boolean,
    val type: String? = null,  // CREATE_PRODUCT, UPDATE_STOCK, etc.
    val data: Map<String, Any>? = null,
    val requiresConfirmation: Boolean = false
)

@RestController
@RequestMapping("/api/comercial")
@Tag(name = "1. Público - Sin Autenticación", description = "Endpoints públicos")
class CommercialChatController(
    private val commercialAssistantService: CommercialAssistantService
) {
    
    @PostMapping("/chat")
    @Operation(
        summary = "Chat Comercial (Público)",
        description = "Consulta sobre planes, precios y características de SIGA. NO requiere autenticación."
    )
    fun chatComercial(@Valid @RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val result = commercialAssistantService.processMessage(request.message)
        
        return result.fold(
            onSuccess = { response ->
                ResponseEntity.ok(ChatResponse(success = true, response = response))
            },
            onFailure = { error ->
                // Convertir Throwable a Exception para que GlobalExceptionHandler lo maneje
                when (error) {
                    is Exception -> throw error
                    else -> throw RuntimeException("Error al procesar la solicitud", error)
                }
            }
        )
    }
}

@RestController
@RequestMapping("/api/saas")
@Tag(name = "4. Gestión Operativa", description = "Requiere autenticación + suscripción activa")
class SaasChatController(
    private val operationalAssistantService: OperationalAssistantService,
    private val subscriptionService: SubscriptionService
) {
    
    @PostMapping("/chat")
    @Operation(
        summary = "Chat Operativo",
        description = "Asistente IA para consultas sobre inventario, stock y ventas de TU negocio. Requiere autenticación + suscripción activa.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun chatSaas(@Valid @RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val userId = SecurityUtils.getUserId()
        if (userId == null || !SecurityUtils.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ChatResponse(success = false, message = "No autenticado"))
        }
        
        val email = SecurityUtils.getUserEmail()
        if (email == null || !subscriptionService.hasActiveSubscription(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ChatResponse(success = false, message = "Se requiere una suscripción activa"))
        }
        
        val result = operationalAssistantService.processMessage(request.message, userId, SecurityUtils.getUserRol())
        
        return result.fold(
            onSuccess = { response ->
                // Detectar si la respuesta indica una acción ejecutada
                val actionInfo = if (response.startsWith("Éxito:") || response.startsWith("Error:")) {
                    // Extraer tipo de acción del mensaje si es posible
                    val actionType = when {
                        response.contains("Producto") && (response.contains("creado") || response.contains("creada")) -> "CREATE_PRODUCT"
                        response.contains("Producto") && response.contains("actualizado") -> "UPDATE_PRODUCT"
                        response.contains("Producto") && response.contains("eliminado") -> "DELETE_PRODUCT"
                        response.contains("Stock") && response.contains("actualizado") -> "UPDATE_STOCK"
                        response.contains("Local") && (response.contains("creado") || response.contains("creada")) -> "CREATE_LOCAL"
                        response.contains("Categoría") && (response.contains("creada") || response.contains("creado")) -> "CREATE_CATEGORIA"
                        else -> null
                    }
                    
                    ActionInfo(
                        executed = response.startsWith("Éxito:"),
                        type = actionType,
                        data = null,
                        requiresConfirmation = false
                    )
                } else if (response.contains("¿Estás seguro")) {
                    // Requiere confirmación
                    ActionInfo(
                        executed = false,
                        type = null,
                        data = null,
                        requiresConfirmation = true
                    )
                } else {
                    null
                }
                
                ResponseEntity.ok(ChatResponse(success = true, response = response, action = actionInfo))
            },
            onFailure = { error ->
                // Convertir Throwable a Exception para que GlobalExceptionHandler lo maneje
                when (error) {
                    is Exception -> throw error
                    else -> throw RuntimeException("Error al procesar la solicitud", error)
                }
            }
        )
    }
}

