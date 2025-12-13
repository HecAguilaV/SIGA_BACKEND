package com.siga.backend.controller

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    
    private var serverReady = false
    
    @EventListener
    fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
        serverReady = true
    }
    
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        // Responder inmediatamente cuando el servidor web esté listo
        return ResponseEntity.ok(mapOf(
            "status" to if (serverReady) "UP" else "STARTING",
            "service" to "siga-backend"
        ))
    }
}
