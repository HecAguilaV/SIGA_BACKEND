package com.siga.backend.controller

import com.siga.backend.service.CommercialAssistantService
import com.siga.backend.service.OperationalAssistantService
import com.siga.backend.service.SubscriptionService
import com.siga.backend.utils.SecurityUtils
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
    val message: String? = null
)

@RestController
@RequestMapping("/api/comercial")
class CommercialChatController(
    private val commercialAssistantService: CommercialAssistantService
) {
    
    @PostMapping("/chat")
    fun chatComercial(@Valid @RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            val result = commercialAssistantService.processMessage(request.message)
            
            result.fold(
                onSuccess = { response ->
                    ResponseEntity.ok(ChatResponse(success = true, response = response))
                },
                onFailure = { error ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ChatResponse(success = false, message = "Error: ${error.message}"))
                }
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ChatResponse(success = false, message = "Error: ${e.message}"))
        }
    }
}

@RestController
@RequestMapping("/api/saas")
class SaasChatController(
    private val operationalAssistantService: OperationalAssistantService,
    private val subscriptionService: SubscriptionService
) {
    
    @PostMapping("/chat")
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
        
        return try {
            val result = operationalAssistantService.processMessage(request.message, userId, SecurityUtils.getUserRol())
            
            result.fold(
                onSuccess = { response ->
                    ResponseEntity.ok(ChatResponse(success = true, response = response))
                },
                onFailure = { error ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ChatResponse(success = false, message = "Error: ${error.message}"))
                }
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ChatResponse(success = false, message = "Error: ${e.message}"))
        }
    }
}

