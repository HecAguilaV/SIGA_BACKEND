package com.siga.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    
    @Value("\${server.port:8080}")
    private val serverPort: Int
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
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
        
        // Configurar servidores: usar HTTPS en producción, HTTP en local
        val servers = mutableListOf<Server>()
        
        // Servidor de producción (HTTPS)
        val productionServer = Server()
        productionServer.url = "https://siga-backend-production.up.railway.app"
        productionServer.description = "Servidor de producción"
        servers.add(productionServer)
        
        // Servidor local (HTTP) solo si no estamos en producción
        if (serverPort == 8080) {
            val localServer = Server()
            localServer.url = "http://localhost:8080"
            localServer.description = "Servidor local"
            servers.add(localServer)
        }
        
        openAPI.servers = servers
        return openAPI
    }
}

