package com.siga.backend

import com.siga.backend.config.*
import com.siga.backend.api.auth.*
import com.siga.backend.api.chat.*
import com.siga.backend.api.productos.*
import com.siga.backend.api.stock.*
import com.siga.backend.api.ventas.*
import com.siga.backend.api.planes.*
import com.siga.backend.api.suscripciones.*
import com.siga.backend.api.*
import io.ktor.server.application.*
import io.ktor.server.testing.*

/**
 * Configuración de la aplicación para testing
 */
fun Application.testModule() {
    // Configurar CORS
    configureCORS()
    
    // Configurar serialización JSON
    configureSerialization()
    
    // Configurar autenticación JWT
    configureJWTAuth()
    
    // Configurar base de datos (usar base de datos de test)
    configureDatabase()
    
    // Configurar rutas
    configureAuthRoutes()
    configureExampleProtectedRoutes()
    configureCommercialChatRoutes()
    configureSaasChatRoutes()
    configureProductosRoutes()
    configureStockRoutes()
    configureVentasRoutes()
    configurePlanesRoutes()
    configureSuscripcionesRoutes()
}

