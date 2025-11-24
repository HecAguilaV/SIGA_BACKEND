package com.siga.backend.database

import com.siga.backend.config.DatabaseConfig
import com.siga.backend.utils.EnvLoader
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

/**
 * Script para verificar que las tablas se crearon correctamente
 */
fun main() {
    println("🔍 Verificando tablas en la base de datos...")
    
    // Cargar variables de entorno
    EnvLoader.load()
    
    // Inicializar conexión
    DatabaseConfig.init()
    
    transaction {
        println("\n📊 Esquemas encontrados:")
        val schemasList = mutableListOf<String>()
        exec("SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('siga_saas', 'siga_comercial', 'public') ORDER BY schema_name") { resultSet ->
            while (resultSet.next()) {
                schemasList.add(resultSet.getString("schema_name"))
            }
        }
        schemasList.forEach { println("  ✅ $it") }
        
        println("\n📋 Tablas en siga_saas:")
        val saasTablesList = mutableListOf<String>()
        exec("SELECT table_name FROM information_schema.tables WHERE table_schema = 'siga_saas' ORDER BY table_name") { resultSet ->
            while (resultSet.next()) {
                saasTablesList.add(resultSet.getString("table_name"))
            }
        }
        if (saasTablesList.isEmpty()) {
            println("  ❌ No se encontraron tablas")
        } else {
            saasTablesList.forEach { println("  ✅ $it") }
        }
        
        println("\n📋 Tablas en siga_comercial:")
        val comercialTablesList = mutableListOf<String>()
        exec("SELECT table_name FROM information_schema.tables WHERE table_schema = 'siga_comercial' ORDER BY table_name") { resultSet ->
            while (resultSet.next()) {
                comercialTablesList.add(resultSet.getString("table_name"))
            }
        }
        if (comercialTablesList.isEmpty()) {
            println("  ❌ No se encontraron tablas")
        } else {
            comercialTablesList.forEach { println("  ✅ $it") }
        }
        
        println("\n📊 Resumen:")
        println("  siga_saas: ${saasTablesList.size} tablas")
        println("  siga_comercial: ${comercialTablesList.size} tablas")
    }
    
    DatabaseConfig.close()
    println("\n✨ Verificación completada")
}
