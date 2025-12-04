package com.siga.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        // Detectar si estamos en producción
        val isProduction = System.getenv("RAILWAY_ENVIRONMENT") != null || 
                          System.getenv("RAILWAY_PUBLIC_DOMAIN") != null
        
        val servers = mutableListOf<Server>()
        
        if (isProduction) {
            // En producción, solo usar servidor HTTPS
            val productionServer = Server()
                .url("https://siga-backend-production.up.railway.app")
                .description("Servidor de producción")
            servers.add(productionServer)
        } else {
            // En desarrollo, usar localhost primero
            val localServer = Server()
                .url("http://localhost:8080")
                .description("Servidor local")
            servers.add(localServer)
        }
        
        // Configurar esquema de seguridad JWT
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
        
        val securityRequirement = SecurityRequirement().addList("bearerAuth")
        
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
            .servers(servers)
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("bearerAuth", securityScheme)
            )
            .addSecurityItem(securityRequirement)
    }
}

