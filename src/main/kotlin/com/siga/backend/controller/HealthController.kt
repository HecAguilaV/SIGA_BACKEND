package com.siga.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthResponse(
    val status: String,
    val timestamp: String
)

@RestController
class HealthController {
    
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        // Health check simple que siempre responde
        // No depende de BD para que Railway pueda verificar que la app está corriendo
        // Acepta requests de healthcheck.railway.app
        val response = HealthResponse(
            status = "healthy",
            timestamp = Instant.now().toString()
        )
        return ResponseEntity.ok(response)
    }
}

