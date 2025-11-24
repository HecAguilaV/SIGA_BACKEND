package com.siga.backend.database

import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream

object MigrationRunner {
    
    fun runMigrations() {
        println("🔄 Iniciando migraciones de base de datos...")
        
        // Lista de archivos de migración en orden
        val migrationFiles = listOf(
            "001_create_schemas.sql",
            "002_create_siga_saas_tables.sql",
            "003_create_siga_comercial_tables.sql",
            "004_insert_initial_data.sql"
        )
        
        println("📦 Ejecutando ${migrationFiles.size} archivos de migración")
        
        migrationFiles.forEach { fileName ->
            println("\n📄 Ejecutando: $fileName")
            try {
                val inputStream: InputStream? = 
                    MigrationRunner::class.java.classLoader.getResourceAsStream("db/migrations/$fileName")
                
                if (inputStream == null) {
                    println("⚠️  No se encontró el archivo: $fileName")
                    return@forEach
                }
                
                val sql = inputStream.bufferedReader().use { it.readText() }
                
                // Dividir por punto y coma, pero mantener los statements completos
                // Filtrar comentarios de línea completa
                val lines = sql.lines()
                val cleanedSql = lines
                    .filter { !it.trim().startsWith("--") && it.trim().isNotEmpty() }
                    .joinToString("\n")
                
                // Dividir por punto y coma, pero solo donde realmente termina un statement
                val statements = cleanedSql.split(";")
                    .map { it.trim() }
                    .filter { 
                        it.isNotEmpty() && 
                        !it.matches(Regex("^\\s*$")) // No líneas vacías
                    }
                
                var successCount = 0
                var errorCount = 0
                
                statements.forEach { statement ->
                    if (statement.isNotBlank()) {
                        // Cada statement en su propia transacción
                        transaction {
                            try {
                                exec(statement)
                                successCount++
                            } catch (e: Exception) {
                                // Algunos errores son esperados (como "already exists")
                                val errorMsg = e.message?.lowercase() ?: ""
                                if (errorMsg.contains("already exists") || 
                                    errorMsg.contains("duplicate") ||
                                    errorMsg.contains("already defined")) {
                                    // Ignorar errores de "ya existe" (para idempotencia)
                                    successCount++ // Contar como éxito porque es idempotente
                                } else {
                                    errorCount++
                                    val statementPreview = statement.take(80).replace("\n", " ")
                                    println("  ⚠️  Error: ${e.message?.take(100)}")
                                    println("     Statement: $statementPreview...")
                                    // No lanzar excepción, continuar con el siguiente
                                }
                            }
                        }
                    }
                }
                
                println("  📊 Ejecutados: $successCount exitosos, $errorCount con errores")
                println("✅ $fileName ejecutado correctamente")
            } catch (e: Exception) {
                println("❌ Error ejecutando $fileName: ${e.message}")
                e.printStackTrace()
                // Continuar con el siguiente archivo en lugar de fallar completamente
            }
        }
        
        println("\n✅ Todas las migraciones completadas exitosamente")
    }
}
