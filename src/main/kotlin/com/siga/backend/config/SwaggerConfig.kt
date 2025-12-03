package com.siga.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        // Servidor de producción (HTTPS) - primero para que sea el predeterminado
        val productionServer = Server()
            .url("https://siga-backend-production.up.railway.app")
            .description("Servidor de producción")
        
        // Servidor local (HTTP) - solo para desarrollo
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
            // Servidor de producción primero para que sea el predeterminado
            .servers(listOf(productionServer, localServer))
    }
    
    @Bean
    fun openApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            // Forzar uso de servidor HTTPS en producción
            // Asegurarse de que el primer servidor (producción) siempre use HTTPS
            val servers = openApi.servers
            if (servers != null && servers.isNotEmpty()) {
                val firstServer = servers[0]
                // Si el servidor contiene railway.app, forzar HTTPS
                if (firstServer.url.contains("railway.app") && !firstServer.url.startsWith("https://")) {
                    firstServer.url = firstServer.url.replace("http://", "https://")
                }
            }
        }
    }
}

