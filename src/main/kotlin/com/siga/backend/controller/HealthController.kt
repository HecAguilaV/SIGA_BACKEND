package com.siga.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthResponse(
    val status: String,
    val timestamp: String
)

@RestController
@Tag(name = "1. Público - Sin Autenticación", description = "Endpoints públicos")
class HealthController {
    
    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Verifica que el servidor esté funcionando. Retorna status 'healthy' y timestamp actual. Railway usa este endpoint para verificar que la aplicación está corriendo. NO requiere autenticación."
    )
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

