package com.siga.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.siga.backend.config.*
import com.siga.backend.api.auth.*
import com.siga.backend.api.*
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
    
    // Configurar rutas
    configureRoutes()
}

fun Application.configureRoutes() {
    // Rutas de autenticación (públicas)
    configureAuthRoutes()
    
    // Rutas de ejemplo para probar el middleware (temporal)
    configureExampleProtectedRoutes()
    
    // Rutas de asistentes
    // comercialChatRoutes()
    // saasChatRoutes()
    
    // Otras rutas
    // productosRoutes()
}
