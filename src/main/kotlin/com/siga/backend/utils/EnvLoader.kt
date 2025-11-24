package com.siga.backend.utils

import java.io.File

object EnvLoader {
    fun load() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.readLines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        // Guardar como propiedad del sistema (se puede modificar)
                        System.setProperty(key, value)
                    }
                }
            }
        }
    }
    
    // Helper para obtener variables: primero env, luego properties
    fun getEnv(key: String, defaultValue: String? = null): String? {
        return System.getenv(key) ?: System.getProperty(key) ?: defaultValue
    }
}
