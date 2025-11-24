package com.siga.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    private lateinit var dataSource: HikariDataSource
    
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL") 
            ?: System.getProperty("DATABASE_URL")
            ?: "jdbc:postgresql://postgresql-hector.alwaysdata.net:5432/hector_siga_db"
        val dbUser = System.getenv("DB_USER") 
            ?: System.getProperty("DB_USER")
            ?: "hector"
        val dbPassword = System.getenv("DB_PASSWORD") 
            ?: System.getProperty("DB_PASSWORD")
            ?: "kike4466"
        
        val config = HikariConfig().apply {
            jdbcUrl = databaseUrl
            username = dbUser
            password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        }
        
        dataSource = HikariDataSource(config)
        
        Database.connect(dataSource)
        
        // Crear esquemas si no existen
        transaction {
            exec("CREATE SCHEMA IF NOT EXISTS siga_saas")
            exec("CREATE SCHEMA IF NOT EXISTS siga_comercial")
        }
        
        println("✅ Base de datos conectada: $databaseUrl")
    }
    
    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}

fun Application.configureDatabase() {
    DatabaseConfig.init()
}
