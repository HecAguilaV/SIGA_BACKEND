package com.siga.backend.utils

object ErrorSanitizer {
    /**
     * Sanitiza mensajes de error para no exponer información sensible.
     * Elimina API keys, URLs con credenciales, stack traces, etc.
     */
    fun sanitize(errorMessage: String?): String {
        if (errorMessage == null) {
            return "Error interno del servidor"
        }
        
        var sanitized = errorMessage
        
        // Eliminar API keys de URLs (patrón: ?key=... o &key=...)
        sanitized = sanitized.replace(Regex("""[?&]key=[A-Za-z0-9_-]+"""), "?key=***")
        
        // Eliminar tokens JWT (patrón: eyJ...)
        sanitized = sanitized.replace(Regex("""eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+"""), "***")
        
        // Eliminar URLs completas con credenciales
        sanitized = sanitized.replace(Regex("""https?://[^/]+/[^\s]+""")) { matchResult ->
            val url = matchResult.value
            if (url.contains("key=") || url.contains("token=") || url.contains("api_key=")) {
                "https://***"
            } else {
                url
            }
        }
        
        // Eliminar stack traces (líneas que empiezan con "at " o contienen "Exception:")
        sanitized = sanitized.lines()
            .filterNot { it.trim().startsWith("at ") || it.contains("Exception:") || it.contains("Caused by:") }
            .joinToString("\n")
        
        // Si el mensaje está vacío o es muy técnico, devolver mensaje genérico
        if (sanitized.isBlank() || sanitized.length > 500) {
            return "Error al procesar la solicitud. Por favor, intente más tarde."
        }
        
        // Limitar longitud del mensaje
        return if (sanitized.length > 200) {
            sanitized.take(200) + "..."
        } else {
            sanitized
        }
    }
    
    /**
     * Sanitiza excepciones completas, extrayendo solo el mensaje sanitizado.
     */
    fun sanitizeException(exception: Throwable): String {
        val message = exception.message ?: exception.javaClass.simpleName
        return sanitize(message)
    }
}

