package com.siga.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        // Configuración mínima - solo lo esencial
        val productionServer = Server()
            .url("https://siga-backend-production.up.railway.app")
            .description("Servidor de producción")
        
        val localServer = Server()
            .url("http://localhost:8080")
            .description("Servidor local")
        
        return OpenAPI()
            .info(
                Info()
                    .title("SIGA Backend API")
                    .version("1.0.0")
                    .description("API REST para el Sistema Inteligente de Gestión de Activos (SIGA)")
                    .contact(
                        Contact()
                            .name("SIGA Team")
                            .email("support@siga.com")
                    )
            )
            .servers(listOf(productionServer, localServer))
    }
}

