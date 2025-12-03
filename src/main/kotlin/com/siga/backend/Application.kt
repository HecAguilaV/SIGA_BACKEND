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
    
    // Railway asigna PORT automáticamente, leer de variable de entorno del sistema
    val port = System.getenv("PORT")?.toIntOrNull() 
        ?: EnvLoader.getEnv("PORT")?.toIntOrNull() 
        ?: 8080
    
    println("🚀 Iniciando servidor SIGA Backend en puerto $port")
    println("📡 Host: 0.0.0.0")
    
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    println("⚙️ Configurando módulos de la aplicación...")
    
    // Configurar CORS
    configureCORS()
    println("✅ CORS configurado")
    
    // Configurar serialización JSON
    configureSerialization()
    println("✅ Serialización configurada")
    
    // Configurar autenticación JWT (middleware)
    configureJWTAuth()
    println("✅ JWT Auth configurado")
    
    // Configurar base de datos
    try {
        configureDatabase()
        println("✅ Base de datos configurada")
    } catch (e: Exception) {
        println("⚠️ Error configurando base de datos: ${e.message}")
        // Continuamos aunque falle la DB para que el health check funcione
    }
    
    // Configurar OpenAPI/Swagger
    configureOpenAPI()
    println("✅ OpenAPI configurado")
    
    // Configurar rutas
    configureHealthRoutes() // Health check debe ir primero
    println("✅ Health check configurado")
    configureRoutes()
    println("✅ Rutas configuradas")
    
    println("🎉 Aplicación lista para recibir requests")
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
