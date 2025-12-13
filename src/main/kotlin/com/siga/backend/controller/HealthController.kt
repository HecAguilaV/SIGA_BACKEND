package com.siga.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        // Responder inmediatamente sin depender de ningún estado
        // Railway solo necesita una respuesta HTTP 200
        return ResponseEntity.ok(mapOf("status" to "UP"))
    }
}
