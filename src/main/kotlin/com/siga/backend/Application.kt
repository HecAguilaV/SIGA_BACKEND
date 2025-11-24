package com.siga.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.siga.backend.config.*
import com.siga.backend.api.auth.*
import com.siga.backend.api.chat.*
import com.siga.backend.api.productos.*
import com.siga.backend.api.stock.*
import com.siga.backend.api.planes.*
import com.siga.backend.api.ventas.*
import com.siga.backend.api.suscripciones.*
import com.siga.backend.api.health.*
import com.siga.backend.config.*
import com.siga.backend.utils.EnvLoader

fun main() {
    // Cargar variables de entorno desde .env
    EnvLoader.load()
    
    val port = EnvLoader.getEnv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configurar CORS
    configureCORS()
    
    // Configurar serialización JSON
    configureSerialization()
    
    // Configurar autenticación JWT (middleware)
    configureJWTAuth()
    
    // Configurar base de datos
    configureDatabase()
    
    // Configurar OpenAPI/Swagger
    configureOpenAPI()
    
    // Configurar rutas
    configureHealthRoutes() // Health check debe ir primero
    configureRoutes()
}

fun Application.configureRoutes() {
    // Rutas de autenticación (públicas)
    configureAuthRoutes()
    
    // Rutas de asistentes IA
    configureCommercialChatRoutes()
    configureSaasChatRoutes()
    
    // Rutas CRUD
    configureProductosRoutes()
    configureStockRoutes()
    configureVentasRoutes()
    configurePlanesRoutes()
    configureSuscripcionesRoutes()
}
