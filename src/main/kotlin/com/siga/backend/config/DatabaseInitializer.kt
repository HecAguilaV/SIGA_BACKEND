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
        // No lanzar excepción para permitir que la aplicación inicie incluso si hay problemas de BD
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siga_saas")
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS siga_comercial")
            System.out.println("Esquemas de base de datos inicializados correctamente")
        } catch (e: Exception) {
            // Log del error pero no detener la aplicación
            System.err.println("ADVERTENCIA: Error al inicializar esquemas: ${e.message}")
            System.err.println("La aplicación continuará iniciando, pero algunos endpoints pueden fallar")
            // No hacer throw para permitir que la app inicie
        }
    }
}

