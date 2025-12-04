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
        // Detectar si estamos en producción (Railway)
        val isProduction = System.getenv("RAILWAY_ENVIRONMENT") != null || 
                          System.getenv("RAILWAY_PUBLIC_DOMAIN") != null
        
        val servers = mutableListOf<Server>()
        
        if (isProduction) {
            // En producción, solo usar HTTPS
            val productionServer = Server()
                .url("https://siga-backend-production.up.railway.app")
                .description("Servidor de producción")
            servers.add(productionServer)
        } else {
            // En desarrollo, usar localhost
            val localServer = Server()
                .url("http://localhost:8080")
                .description("Servidor local")
            servers.add(localServer)
        }
        
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
    }
    
    @Bean
    fun openApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            // Detectar si estamos en producción
            val isProduction = System.getenv("RAILWAY_ENVIRONMENT") != null || 
                              System.getenv("RAILWAY_PUBLIC_DOMAIN") != null
            
            if (isProduction) {
                // En producción, reemplazar TODOS los servidores con HTTPS
                val httpsServer = Server()
                    .url("https://siga-backend-production.up.railway.app")
                    .description("Servidor de producción")
                
                // Reemplazar la lista de servidores con solo el servidor HTTPS
                openApi.servers = listOf(httpsServer)
                
                // También forzar HTTPS en todas las operaciones
                openApi.paths?.forEach { (path, pathItem) ->
                    pathItem.readOperationsMap().forEach { (_, operation) ->
                        operation.servers?.forEach { server ->
                            if (server.url.startsWith("http://")) {
                                server.url = server.url.replace("http://", "https://")
                            }
                            if (server.url.contains("railway.app") && !server.url.contains("siga-backend-production")) {
                                server.url = "https://siga-backend-production.up.railway.app"
                            }
                        }
                    }
                }
            }
        }
    }
}

