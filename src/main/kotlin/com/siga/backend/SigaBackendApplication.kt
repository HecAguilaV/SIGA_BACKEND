package com.siga.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        // Excluir validación de BD al inicio para permitir healthcheck rápido
        // org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration::class
    ]
)
class SigaBackendApplication

fun main(args: Array<String>) {
    // Log del puerto al iniciar para debugging
    val port = System.getenv("PORT") ?: "8080"
    println("========================================")
    println("Iniciando SIGA Backend")
    println("Puerto: $port")
    println("========================================")
    runApplication<SigaBackendApplication>(*args)
}

