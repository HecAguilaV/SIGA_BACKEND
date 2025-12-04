package com.siga.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
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
        
        // Definir tags en orden lógico del flujo del usuario
        // IMPORTANTE: El orden aquí define el orden en Swagger UI
        val tags = listOf(
            Tag().name("1. Público - Sin Autenticación")
                .description("Endpoints públicos que no requieren autenticación. Perfectos para empezar."),
            Tag().name("2. Autenticación")
                .description("Registro y login de usuarios operativos (ADMINISTRADOR, OPERADOR, CAJERO)"),
            Tag().name("3. Portal Comercial")
                .description("Gestión de planes y suscripciones. Requiere autenticación como Usuario Comercial"),
            Tag().name("4. Gestión Operativa")
                .description("Productos, stock, ventas y chat operativo. Requiere autenticación + suscripción activa"),
            Tag().name("5. Administración")
                .description("Endpoints administrativos para gestión de usuarios y configuración del sistema")
        )
        
        return OpenAPI()
            .info(
                Info()
                    .title("SIGA Backend API")
                    .version("1.0.0")
                    .description("""
                        API REST para el Sistema Inteligente de Gestión de Activos (SIGA)
                        
                        **Flujo recomendado:**
                        1. Explora endpoints públicos (planes, chat comercial)
                        2. Regístrate o inicia sesión
                        3. Crea una suscripción
                        4. Usa los endpoints operativos (productos, ventas, chat operativo)
                        5. Accede a funciones administrativas (gestión de usuarios)
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("SIGA Team")
                            .email("support@siga.com")
                    )
            )
            .servers(servers)
            .tags(tags)
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("bearerAuth", securityScheme)
            )
            .addSecurityItem(securityRequirement)
    }
}

