package com.siga.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthResponse(
    val status: String,
    val database: String,
    val timestamp: String
)

@RestController
class HealthController(
    private val jdbcTemplate: JdbcTemplate
) {
    
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        val dbStatus = try {
            jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            "connected"
        } catch (e: Exception) {
            "disconnected"
        }
        
        return ResponseEntity.ok(
            HealthResponse(
                status = "healthy",
                database = dbStatus,
                timestamp = Instant.now().toString()
            )
        )
    }
}

