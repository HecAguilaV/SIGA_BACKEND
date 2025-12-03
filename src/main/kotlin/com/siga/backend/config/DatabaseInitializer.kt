package com.siga.backend.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class DatabaseInitializer(
    @Autowired private val jdbcTemplate: JdbcTemplate
) {
    
    @PostConstruct
    fun init() {
        // Crear esquemas si no existen
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siga_saas")
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siga_comercial")
            println("Esquemas de base de datos inicializados correctamente")
        } catch (e: Exception) {
            println("Error al inicializar esquemas: ${e.message}")
            e.printStackTrace()
        }
    }
}

