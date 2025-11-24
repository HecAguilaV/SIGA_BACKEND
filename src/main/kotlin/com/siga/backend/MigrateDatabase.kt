package com.siga.backend

import com.siga.backend.config.DatabaseConfig
import com.siga.backend.database.MigrationRunner
import com.siga.backend.utils.EnvLoader

/**
 * Script para ejecutar migraciones de base de datos
 * 
 * Ejecutar desde IntelliJ IDEA o con:
 * ./gradlew run --args="migrate"
 */
fun main(args: Array<String>) {
    println("🚀 Ejecutando migraciones de base de datos...")
    
    // Cargar variables de entorno
    EnvLoader.load()
    
    // Inicializar conexión a base de datos
    DatabaseConfig.init()
    
    // Ejecutar migraciones
    MigrationRunner.runMigrations()
    
    // Cerrar conexión
    DatabaseConfig.close()
    
    println("✨ Proceso completado")
}
