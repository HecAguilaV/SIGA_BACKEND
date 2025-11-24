package com.siga.backend

import com.siga.backend.config.DatabaseConfig
import com.siga.backend.utils.EnvLoader
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

/**
 * Script para verificar la conexión a la base de datos
 */
fun main() {
    println("Verificando conexion a la base de datos...")
    
    try {
        // Cargar variables de entorno
        EnvLoader.load()
        
        val databaseUrl = EnvLoader.getEnv("DATABASE_URL")
        val dbUser = EnvLoader.getEnv("DB_USER")
        
        println("\nConfiguracion:")
        println("  URL: ${databaseUrl?.replace(Regex(":[^:]*@"), ":****@")}") // Ocultar password
        println("  Usuario: $dbUser")
        
        // Inicializar conexión
        println("\nConectando...")
        DatabaseConfig.init()
        
        // Verificar conexión con una query simple
        transaction {
            var version: String? = null
            exec("SELECT version()") { resultSet ->
                if (resultSet.next()) {
                    version = resultSet.getString(1)
                }
            }
            
            println("\nConexion exitosa!")
            if (version != null) {
                println("Version PostgreSQL: ${version!!.substring(0, minOf(50, version!!.length))}...")
            }
            
            // Verificar esquemas
            println("\nVerificando esquemas...")
            val schemas = mutableListOf<String>()
            exec("SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('siga_saas', 'siga_comercial') ORDER BY schema_name") { resultSet ->
                while (resultSet.next()) {
                    schemas.add(resultSet.getString("schema_name"))
                }
            }
            
            if (schemas.isEmpty()) {
                println("  ADVERTENCIA: No se encontraron los esquemas siga_saas o siga_comercial")
                println("  Ejecuta: ./gradlew migrate")
            } else {
                schemas.forEach { println("  OK: $it") }
            }
        }
        
        DatabaseConfig.close()
        println("\nVerificacion completada exitosamente")
        
    } catch (e: Exception) {
        println("\nERROR: No se pudo conectar a la base de datos")
        println("Detalle: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}

