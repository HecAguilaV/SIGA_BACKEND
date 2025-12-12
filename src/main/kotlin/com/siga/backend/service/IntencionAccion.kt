package com.siga.backend.service

/**
 * Enum que representa las intenciones que el asistente puede detectar y ejecutar
 */
enum class IntencionAccion {
    // Consultas (ya funcionan)
    CONSULTAR,
    LISTAR,
    
    // Productos
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DELETE_PRODUCT,
    
    // Stock
    UPDATE_STOCK,
    
    // Ventas
    CREATE_VENTA,
    
    // Locales
    CREATE_LOCAL,
    UPDATE_LOCAL,
    DELETE_LOCAL,
    
    // Categorías
    CREATE_CATEGORIA,
    UPDATE_CATEGORIA,
    DELETE_CATEGORIA,
    
    // Acción desconocida o no reconocida
    UNKNOWN
}

/**
 * Data class que representa una intención detectada con sus parámetros
 */
data class IntencionDetectada(
    val intencion: IntencionAccion,
    val entidad: String, // "producto", "stock", "venta", "local", "categoria"
    val parametros: Map<String, Any> = emptyMap(),
    val requiereConfirmacion: Boolean = false,
    val mensajeConfirmacion: String? = null
)
