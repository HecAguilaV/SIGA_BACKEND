package com.siga.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.siga.backend.entity.*
import com.siga.backend.repository.*
import com.siga.backend.config.ApplicationContextProvider
import com.siga.backend.utils.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class OperationalAssistantService(
    private val geminiService: GeminiService,
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val productoRepository: ProductoRepository,
    private val stockRepository: StockRepository,
    private val localRepository: LocalRepository,
    private val categoriaRepository: CategoriaRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(OperationalAssistantService::class.java)
    
    private val systemContext = """
        Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
        Ayudas a usuarios a gestionar su inventario, consultar stock, ver ventas, etc.
        
        Responde de forma amigable y profesional en español.
        Usa los datos proporcionados en el contexto para dar respuestas precisas.
        
        Si no tienes información suficiente en el contexto, indica que necesitas más datos.
        Sé conciso pero completo en tus respuestas.
    """.trimIndent()
    
    private val intencionDetectionPrompt = """
        Eres un analizador de intenciones para SIGA. Tu ÚNICA tarea es analizar el mensaje del usuario y retornar SOLO un JSON válido, sin texto adicional.
        
        IMPORTANTE: Responde ÚNICAMENTE con el JSON, sin explicaciones, sin texto antes o después.
        
        Analiza el mensaje y retorna este JSON exacto:
        {
            "intencion": "CREATE_PRODUCT" | "UPDATE_PRODUCT" | "DELETE_PRODUCT" | "UPDATE_STOCK" | "CREATE_VENTA" | "CREATE_LOCAL" | "CREATE_CATEGORIA" | "CONSULTAR" | "LISTAR" | "UNKNOWN",
            "entidad": "producto" | "stock" | "venta" | "local" | "categoria" | "",
            "parametros": {
                "nombre": "valor si existe",
                "precio": "valor si existe",
                "cantidad": número si existe,
                "local": "nombre del local si existe",
                "producto": "nombre del producto si existe",
                "id": número si existe
            },
            "requiereConfirmacion": true o false
        }
        
        Reglas de detección:
        - Si el mensaje contiene "crea", "crear", "agrega", "añade" + "producto" → CREATE_PRODUCT
        - Si el mensaje contiene "actualiza", "modifica", "cambia" + "producto" → UPDATE_PRODUCT
        - Si el mensaje contiene "elimina", "borra", "quita" + "producto" → DELETE_PRODUCT (requiereConfirmacion: true)
        - Si el mensaje contiene "stock", "inventario", "cantidad" + ("agrega", "añade", "actualiza", "pon", "poner") → UPDATE_STOCK
        - Si el mensaje contiene "crea", "crear" + "local" → CREATE_LOCAL
        - Si el mensaje contiene "crea", "crear" + "categoría" o "categoria" → CREATE_CATEGORIA
        - Si el mensaje contiene "¿", "qué", "cuánto", "muestra", "lista" → CONSULTAR o LISTAR
        - Si no coincide con nada → UNKNOWN
        
        Extrae parámetros del mensaje:
        - "precio 1500" → "precio": "1500"
        - "llamado Café" o "producto Café" → "nombre": "Café"
        - "50 unidades" o "cinco" o "5" → "cantidad": 50 o 5
        - "local ITR" o "al local ITR" → "local": "ITR"
        - "cinco mantequillas" → "cantidad": 5, "producto": "mantequillas"
        - "agregar cinco mantequillas al local the House" → "cantidad": 5, "producto": "mantequillas", "local": "the House"
        
        EJEMPLO: "Crea un producto llamado Café con precio 1500"
        RESPUESTA: {"intencion":"CREATE_PRODUCT","entidad":"producto","parametros":{"nombre":"Café","precio":"1500"},"requiereConfirmacion":false}
    """.trimIndent()
    
    fun buildRAGContext(userId: Int, userRol: String, query: String): String {
        val context = mutableListOf<String>()
        
        val user = usuarioSaasRepository.findById(userId).orElse(null)
            ?: return "Usuario no encontrado"
        
        context.add("=== INFORMACIÓN DEL USUARIO ===\nUsuario: ${user.nombre} (${user.email})\nRol: $userRol")
        
        // Obtener usuario_comercial_id para filtrar por empresa
        val usuarioComercialId = user.usuarioComercialId ?: run {
            // Si no tiene, buscar por email
            val usuarioComercialRepository = ApplicationContextProvider.getBean(com.siga.backend.repository.UsuarioComercialRepository::class.java)
            usuarioComercialRepository.findByEmail(user.email.lowercase()).orElse(null)?.id
        }
        
        // Filtrar productos por empresa
        val productosQuery = if (usuarioComercialId != null) {
            "SELECT id, nombre, precio_unitario FROM siga_saas.PRODUCTOS WHERE activo = true AND usuario_comercial_id = $usuarioComercialId ORDER BY nombre LIMIT 100"
        } else {
            "SELECT id, nombre, precio_unitario FROM siga_saas.PRODUCTOS WHERE activo = true ORDER BY nombre LIMIT 100"
        }
        val productos = jdbcTemplate.queryForList(productosQuery)
        if (productos.isNotEmpty()) {
            context.add("\n=== PRODUCTOS DISPONIBLES ===")
            productos.forEach { producto ->
                val precio = producto["precio_unitario"] ?: "N/A"
                context.add("${producto["nombre"]} (ID: ${producto["id"]}) - Precio: $${precio}")
            }
        } else {
            context.add("\n=== PRODUCTOS ===")
            context.add("No hay productos registrados aún")
        }
        
        // Filtrar stock por empresa
        val stockQuery = if (usuarioComercialId != null) {
            "SELECT p.nombre, s.cantidad, l.nombre as local FROM siga_saas.STOCK s " +
            "JOIN siga_saas.PRODUCTOS p ON s.producto_id = p.id " +
            "JOIN siga_saas.LOCALES l ON s.local_id = l.id " +
            "WHERE p.activo = true AND p.usuario_comercial_id = $usuarioComercialId AND l.usuario_comercial_id = $usuarioComercialId " +
            "ORDER BY p.nombre, l.nombre " +
            "LIMIT 100"
        } else {
            "SELECT p.nombre, s.cantidad, l.nombre as local FROM siga_saas.STOCK s " +
            "JOIN siga_saas.PRODUCTOS p ON s.producto_id = p.id " +
            "JOIN siga_saas.LOCALES l ON s.local_id = l.id " +
            "WHERE p.activo = true " +
            "ORDER BY p.nombre, l.nombre " +
            "LIMIT 100"
        }
        val stock = jdbcTemplate.queryForList(stockQuery)
        if (stock.isNotEmpty()) {
            context.add("\n=== STOCK ACTUAL ===")
            stock.forEach { item ->
                val cantidad = item["cantidad"] ?: 0
                context.add("${item["nombre"]} en ${item["local"]}: $cantidad unidades")
            }
        } else {
            context.add("\n=== STOCK ===")
            context.add("No hay stock registrado. Los productos existen pero no tienen stock asignado.")
        }
        
        // Agregar información sobre productos sin stock (filtrado por empresa)
        val productosSinStockQuery = if (usuarioComercialId != null) {
            "SELECT p.id, p.nombre FROM siga_saas.PRODUCTOS p " +
            "WHERE p.activo = true AND p.usuario_comercial_id = $usuarioComercialId " +
            "AND NOT EXISTS (SELECT 1 FROM siga_saas.STOCK s WHERE s.producto_id = p.id) " +
            "ORDER BY p.nombre " +
            "LIMIT 50"
        } else {
            "SELECT p.id, p.nombre FROM siga_saas.PRODUCTOS p " +
            "WHERE p.activo = true " +
            "AND NOT EXISTS (SELECT 1 FROM siga_saas.STOCK s WHERE s.producto_id = p.id) " +
            "ORDER BY p.nombre " +
            "LIMIT 50"
        }
        val productosSinStock = jdbcTemplate.queryForList(productosSinStockQuery)
        if (productosSinStock.isNotEmpty()) {
            context.add("\n=== PRODUCTOS SIN STOCK (existen pero no tienen stock asignado) ===")
            productosSinStock.forEach { producto ->
                context.add("${producto["nombre"]} (ID: ${producto["id"]}) - Sin stock")
            }
        }
        
        return context.joinToString("\n\n")
    }
    
    /**
     * Detecta la intención del usuario usando Gemini
     */
    private fun detectarIntencion(mensaje: String): Result<IntencionDetectada> {
        return try {
            val prompt = """
                $intencionDetectionPrompt
                
                MENSAJE DEL USUARIO:
                $mensaje
                
                Responde SOLO con el JSON, sin texto adicional.
            """.trimIndent()
            
            val resultado = geminiService.generateContent(prompt)
            
            resultado.fold(
                onSuccess = { respuesta ->
                    try {
                        // Limpiar respuesta de Gemini (puede incluir markdown o texto adicional)
                        val jsonLimpio = respuesta
                            .replace("```json", "")
                            .replace("```", "")
                            .trim()
                        
                        val jsonNode = objectMapper.readTree(jsonLimpio)
                        
                        val intencionStr = jsonNode.get("intencion")?.asText() ?: "UNKNOWN"
                        val intencion = try {
                            IntencionAccion.valueOf(intencionStr)
                        } catch (e: Exception) {
                            IntencionAccion.UNKNOWN
                        }
                        
                        val entidad = jsonNode.get("entidad")?.asText() ?: ""
                        val parametrosNode = jsonNode.get("parametros") ?: objectMapper.createObjectNode()
                        val parametros = mutableMapOf<String, Any>()
                        
                        parametrosNode.fields().forEach { (key, value) ->
                            when {
                                value.isInt -> parametros[key] = value.asInt()
                                value.isDouble || value.isFloat -> parametros[key] = value.asDouble()
                                value.isBoolean -> parametros[key] = value.asBoolean()
                                else -> parametros[key] = value.asText()
                            }
                        }
                        
                        val requiereConfirmacion = jsonNode.get("requiereConfirmacion")?.asBoolean() ?: false
                        
                        Result.success(
                            IntencionDetectada(
                                intencion = intencion,
                                entidad = entidad,
                                parametros = parametros,
                                requiereConfirmacion = requiereConfirmacion,
                                mensajeConfirmacion = if (requiereConfirmacion) {
                                    "¿Estás seguro de que deseas ${obtenerAccionEnEspanol(intencion)}?"
                                } else null
                            )
                        )
                    } catch (e: Exception) {
                        logger.warn("Error al parsear intención de Gemini: ${e.message}", e)
                        Result.success(IntencionDetectada(IntencionAccion.UNKNOWN, "", emptyMap()))
                    }
                },
                onFailure = { error ->
                    logger.error("Error al detectar intención con Gemini", error)
                    Result.success(IntencionDetectada(IntencionAccion.UNKNOWN, "", emptyMap()))
                }
            )
        } catch (e: Exception) {
            logger.error("Excepción al detectar intención", e)
            Result.success(IntencionDetectada(IntencionAccion.UNKNOWN, "", emptyMap()))
        }
    }
    
    private fun obtenerAccionEnEspanol(intencion: IntencionAccion): String {
        return when (intencion) {
            IntencionAccion.DELETE_PRODUCT -> "eliminar este producto"
            IntencionAccion.DELETE_LOCAL -> "eliminar este local"
            IntencionAccion.DELETE_CATEGORIA -> "eliminar esta categoría"
            else -> "realizar esta acción"
        }
    }
    
    /**
     * Ejecuta una acción según la intención detectada
     */
    private fun ejecutarAccion(intencion: IntencionDetectada, userId: Int, userRol: String?): Result<AccionEjecutada> {
        return try {
            when (intencion.intencion) {
                IntencionAccion.CREATE_PRODUCT -> ejecutarCrearProducto(intencion.parametros, userRol)
                IntencionAccion.UPDATE_PRODUCT -> ejecutarActualizarProducto(intencion.parametros, userRol)
                IntencionAccion.DELETE_PRODUCT -> ejecutarEliminarProducto(intencion.parametros, userRol)
                IntencionAccion.UPDATE_STOCK -> ejecutarActualizarStock(intencion.parametros, userRol)
                IntencionAccion.CREATE_LOCAL -> ejecutarCrearLocal(intencion.parametros, userRol)
                IntencionAccion.CREATE_CATEGORIA -> ejecutarCrearCategoria(intencion.parametros, userRol)
                IntencionAccion.CONSULTAR, IntencionAccion.LISTAR -> {
                    // Para consultas, no ejecutamos acción, solo retornamos que es consulta
                    Result.success(AccionEjecutada(false, "CONSULTA", null, null))
                }
                else -> Result.success(AccionEjecutada(false, "UNKNOWN", null, null))
            }
        } catch (e: Exception) {
            logger.error("Error al ejecutar acción ${intencion.intencion}", e)
            Result.failure(e)
        }
    }
    
    // Métodos de ejecución de acciones específicas
    private fun ejecutarCrearProducto(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeCrearProductos()) {
            return Result.success(AccionEjecutada(false, "CREATE_PRODUCT", null, "No tienes permiso para crear productos"))
        }
        
        val nombre = params["nombre"] as? String ?: return Result.success(
            AccionEjecutada(false, "CREATE_PRODUCT", null, "Se requiere el nombre del producto")
        )
        
        val precioStr = params["precio"] as? String ?: params["precio_unitario"] as? String
        val precio = precioStr?.let { 
            try { BigDecimal(it) } catch (e: Exception) { null }
        }
        
        val categoriaId = (params["categoria_id"] as? Number)?.toInt() ?: (params["categoriaId"] as? Number)?.toInt()
        val descripcion = params["descripcion"] as? String
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        val producto = Producto(
            nombre = nombre,
            descripcion = descripcion,
            categoriaId = categoriaId,
            precioUnitario = precio,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )
        
        val productoGuardado = productoRepository.save(producto)
        
        return Result.success(
            AccionEjecutada(
                true,
                "CREATE_PRODUCT",
                mapOf(
                    "id" to productoGuardado.id,
                    "nombre" to productoGuardado.nombre,
                    "precio" to (productoGuardado.precioUnitario?.toString() ?: "N/A")
                ),
                "Producto '${productoGuardado.nombre}' creado exitosamente"
            )
        )
    }
    
    private fun ejecutarActualizarProducto(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeActualizarProductos()) {
            return Result.success(AccionEjecutada(false, "UPDATE_PRODUCT", null, "No tienes permiso para actualizar productos"))
        }
        
        val id = (params["id"] as? Number)?.toInt() ?: return Result.success(
            AccionEjecutada(false, "UPDATE_PRODUCT", null, "Se requiere el ID del producto")
        )
        
        val producto = productoRepository.findById(id).orElse(null)
            ?: return Result.success(AccionEjecutada(false, "UPDATE_PRODUCT", null, "Producto no encontrado"))
        
        // Verificar que el producto pertenece a la empresa del usuario
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        if (usuarioComercialId != null && producto.usuarioComercialId != usuarioComercialId) {
            return Result.success(AccionEjecutada(false, "UPDATE_PRODUCT", null, "No tienes acceso a este producto"))
        }
        
        val nombre = params["nombre"] as? String ?: producto.nombre
        val precioStr = params["precio"] as? String ?: params["precio_unitario"] as? String
        val precio = precioStr?.let { 
            try { BigDecimal(it) } catch (e: Exception) { null }
        } ?: producto.precioUnitario
        
        val productoActualizado = producto.copy(
            nombre = nombre,
            precioUnitario = precio,
            descripcion = params["descripcion"] as? String ?: producto.descripcion,
            fechaActualizacion = Instant.now()
        )
        
        val productoGuardado = productoRepository.save(productoActualizado)
        
        return Result.success(
            AccionEjecutada(
                true,
                "UPDATE_PRODUCT",
                mapOf(
                    "id" to productoGuardado.id,
                    "nombre" to productoGuardado.nombre
                ),
                "Producto '${productoGuardado.nombre}' actualizado exitosamente"
            )
        )
    }
    
    private fun ejecutarEliminarProducto(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeEliminarProductos()) {
            return Result.success(AccionEjecutada(false, "DELETE_PRODUCT", null, "No tienes permiso para eliminar productos"))
        }
        
        // Obtener usuario_comercial_id para filtrar por empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        val id = (params["id"] as? Number)?.toInt()
            ?: (params["nombre"] as? String)?.let { nombre ->
                val productos = if (usuarioComercialId != null) {
                    productoRepository.findByActivoTrueAndUsuarioComercialId(usuarioComercialId)
                } else {
                    productoRepository.findByActivoTrue()
                }
                productos.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }?.id
            }
            ?: return Result.success(
                AccionEjecutada(false, "DELETE_PRODUCT", null, "Se requiere el ID o nombre del producto")
            )
        
        val producto = productoRepository.findById(id).orElse(null)
            ?: return Result.success(AccionEjecutada(false, "DELETE_PRODUCT", null, "Producto no encontrado"))
        
        // Verificar que el producto pertenece a la empresa del usuario
        if (usuarioComercialId != null && producto.usuarioComercialId != usuarioComercialId) {
            return Result.success(AccionEjecutada(false, "DELETE_PRODUCT", null, "No tienes acceso a este producto"))
        }
        
        val productoEliminado = producto.copy(activo = false, fechaActualizacion = Instant.now())
        productoRepository.save(productoEliminado)
        
        return Result.success(
            AccionEjecutada(
                true,
                "DELETE_PRODUCT",
                mapOf("id" to id, "nombre" to producto.nombre),
                "Producto '${producto.nombre}' eliminado exitosamente"
            )
        )
    }
    
    private fun ejecutarActualizarStock(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeActualizarStock()) {
            return Result.success(AccionEjecutada(false, "UPDATE_STOCK", null, "No tienes permiso para actualizar stock"))
        }
        
        val productoNombre = params["producto"] as? String
        val localNombre = params["local"] as? String
        val cantidad = (params["cantidad"] as? Number)?.toInt()
            ?: return Result.success(
                AccionEjecutada(false, "UPDATE_STOCK", null, "Se requiere la cantidad")
            )
        
        // Obtener usuario_comercial_id para filtrar por empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        // Buscar producto por nombre (búsqueda flexible, filtrado por empresa)
        val producto = productoNombre?.let { nombreBuscar ->
            val productos = if (usuarioComercialId != null) {
                productoRepository.findByActivoTrueAndUsuarioComercialId(usuarioComercialId)
            } else {
                productoRepository.findByActivoTrue()
            }
            // Primero intentar coincidencia exacta (case-insensitive)
            productos.firstOrNull { it.nombre.equals(nombreBuscar, ignoreCase = true) }
                // Si no encuentra, buscar que contenga el nombre
                ?: productos.firstOrNull { it.nombre.contains(nombreBuscar, ignoreCase = true) }
                // Si no encuentra, buscar que el nombre buscado contenga el nombre del producto
                ?: productos.firstOrNull { nombreBuscar.contains(it.nombre, ignoreCase = true) }
        } ?: return Result.success(
            AccionEjecutada(
                false, 
                "UPDATE_STOCK", 
                null, 
                "No encontré el producto '$productoNombre'. ¿Podrías verificar el nombre exacto? Puedes listar los productos disponibles."
            )
        )
        
        // Buscar local por nombre (búsqueda flexible, filtrado por empresa)
        val local = localNombre?.let { nombreBuscar ->
            val locales = if (usuarioComercialId != null) {
                localRepository.findByActivoTrueAndUsuarioComercialId(usuarioComercialId)
            } else {
                localRepository.findByActivoTrue()
            }
            // Primero intentar coincidencia exacta (case-insensitive)
            locales.firstOrNull { it.nombre.equals(nombreBuscar, ignoreCase = true) }
                // Si no encuentra, buscar que contenga el nombre
                ?: locales.firstOrNull { it.nombre.contains(nombreBuscar, ignoreCase = true) }
        } ?: return Result.success(
            AccionEjecutada(
                false, 
                "UPDATE_STOCK", 
                null, 
                "No encontré el local '$localNombre'. ¿Podrías verificar el nombre exacto? Los locales disponibles aparecen en el contexto."
            )
        )
        
        val stockExistente = stockRepository.findByProductoIdAndLocalId(producto.id, local.id)
        
        val stock = if (stockExistente.isPresent) {
            val s = stockExistente.get()
            s.copy(
                cantidad = cantidad,
                fechaActualizacion = Instant.now()
            )
        } else {
            Stock(
                productoId = producto.id,
                localId = local.id,
                cantidad = cantidad,
                cantidadMinima = 0,
                fechaActualizacion = Instant.now()
            )
        }
        
        val stockGuardado = stockRepository.save(stock)
        
        return Result.success(
            AccionEjecutada(
                true,
                "UPDATE_STOCK",
                mapOf(
                    "producto" to producto.nombre,
                    "local" to local.nombre,
                    "cantidad" to stockGuardado.cantidad
                ),
                "Stock actualizado: ${producto.nombre} en ${local.nombre} = ${stockGuardado.cantidad} unidades"
            )
        )
    }
    
    private fun ejecutarCrearLocal(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeCrearLocales()) {
            return Result.success(AccionEjecutada(false, "CREATE_LOCAL", null, "No tienes permiso para crear locales"))
        }
        
        val nombre = params["nombre"] as? String ?: return Result.success(
            AccionEjecutada(false, "CREATE_LOCAL", null, "Se requiere el nombre del local")
        )
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        val local = Local(
            nombre = nombre,
            direccion = params["direccion"] as? String,
            ciudad = params["ciudad"] as? String,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activo = true,
            fechaCreacion = Instant.now()
        )
        
        val localGuardado = localRepository.save(local)
        
        return Result.success(
            AccionEjecutada(
                true,
                "CREATE_LOCAL",
                mapOf("id" to localGuardado.id, "nombre" to localGuardado.nombre),
                "Local '${localGuardado.nombre}' creado exitosamente"
            )
        )
    }
    
    private fun ejecutarCrearCategoria(params: Map<String, Any>, userRol: String?): Result<AccionEjecutada> {
        if (!SecurityUtils.puedeCrearCategorias()) {
            return Result.success(AccionEjecutada(false, "CREATE_CATEGORIA", null, "No tienes permiso para crear categorías"))
        }
        
        val nombre = params["nombre"] as? String ?: return Result.success(
            AccionEjecutada(false, "CREATE_CATEGORIA", null, "Se requiere el nombre de la categoría")
        )
        
        // Obtener usuario_comercial_id para asignar empresa
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
        
        // Verificar que no exista categoría con mismo nombre en la misma empresa
        if (usuarioComercialId != null && categoriaRepository.existsByNombreAndUsuarioComercialId(nombre, usuarioComercialId)) {
            return Result.success(
                AccionEjecutada(false, "CREATE_CATEGORIA", null, "Ya existe una categoría con ese nombre")
            )
        } else if (usuarioComercialId == null && categoriaRepository.existsByNombre(nombre)) {
            return Result.success(
                AccionEjecutada(false, "CREATE_CATEGORIA", null, "Ya existe una categoría con ese nombre")
            )
        }
        
        val categoria = Categoria(
            nombre = nombre,
            descripcion = params["descripcion"] as? String,
            usuarioComercialId = usuarioComercialId, // Asignar empresa
            activa = true,
            fechaCreacion = Instant.now()
        )
        
        val categoriaGuardada = categoriaRepository.save(categoria)
        
        return Result.success(
            AccionEjecutada(
                true,
                "CREATE_CATEGORIA",
                mapOf("id" to categoriaGuardada.id, "nombre" to categoriaGuardada.nombre),
                "Categoría '${categoriaGuardada.nombre}' creada exitosamente"
            )
        )
    }
    
    /**
     * Data class para representar una acción ejecutada
     */
    data class AccionEjecutada(
        val ejecutada: Boolean,
        val tipo: String,
        val datos: Map<String, Any>?,
        val mensaje: String?
    )
    
    fun processMessage(message: String, userId: Int, userRol: String?): Result<String> {
        try {
            logger.debug("Procesando mensaje para usuario $userId con rol ${userRol ?: "OPERADOR"}")
            
            // Paso 1: Detectar intención
            val intencionResult = detectarIntencion(message)
            val intencion = intencionResult.getOrNull() 
                ?: IntencionDetectada(IntencionAccion.UNKNOWN, "", emptyMap())
            
            logger.debug("Intención detectada: ${intencion.intencion}, entidad: ${intencion.entidad}")
            
            // Paso 2: Si requiere confirmación y es DELETE, retornar mensaje de confirmación
            if (intencion.requiereConfirmacion && intencion.intencion.name.startsWith("DELETE")) {
                val mensajeConfirmacion = intencion.mensajeConfirmacion 
                    ?: "¿Estás seguro de que deseas realizar esta acción?"
                return Result.success(mensajeConfirmacion)
            }
            
            // Paso 3: Si es una acción CRUD (no CONSULTAR/LISTAR), ejecutarla
            if (intencion.intencion != IntencionAccion.CONSULTAR && 
                intencion.intencion != IntencionAccion.LISTAR && 
                intencion.intencion != IntencionAccion.UNKNOWN) {
                
                val accionResult = ejecutarAccion(intencion, userId, userRol)
                
                accionResult.fold(
                    onSuccess = { accion ->
                        if (accion.ejecutada) {
                            // Acción ejecutada exitosamente
                            val respuesta = accion.mensaje ?: "Acción ejecutada exitosamente"
                            return Result.success("Éxito: $respuesta")
                        } else {
                            // Error en la ejecución - retornar mensaje sin emoji para evitar problemas de parsing
                            val errorMsg = accion.mensaje ?: "No se pudo ejecutar la acción"
                            return Result.success("Error: $errorMsg")
                        }
                    },
                    onFailure = { error ->
                        logger.error("Error al ejecutar acción", error)
                        return Result.success("Error al ejecutar la acción: ${error.message ?: "Error desconocido"}")
                    }
                )
            }
            
            // Paso 4: Si es consulta o listado, verificar permisos para análisis IA
            // Si la consulta requiere análisis (reportes, gráficos, productos más/menos vendidos)
            val requiereAnalisis = message.contains("reporte", ignoreCase = true) ||
                    message.contains("análisis", ignoreCase = true) ||
                    message.contains("gráfico", ignoreCase = true) ||
                    message.contains("más vendido", ignoreCase = true) ||
                    message.contains("menos vendido", ignoreCase = true) ||
                    message.contains("estadística", ignoreCase = true)
            
            if (requiereAnalisis && !SecurityUtils.puedeAnalisisIA()) {
                return Result.success(
                    "No tienes permiso para solicitar análisis. " +
                    "Contacta al administrador para que te asigne el permiso de análisis."
                )
            }
            
            // Verificar permiso básico para usar asistente
            if (!SecurityUtils.puedeUsarAsistente()) {
                return Result.success("No tienes permiso para usar el asistente. Contacta al administrador.")
            }
            
            // Paso 5: Si es consulta o listado, usar el flujo original con Gemini
            val ragContext = buildRAGContext(userId, userRol ?: "OPERADOR", message)
            logger.debug("Contexto RAG construido: ${ragContext.length} caracteres")
            
            val fullPrompt = """
            $systemContext
            
            CONTEXTO DE DATOS:
            $ragContext
            
            PREGUNTA DEL USUARIO:
            $message
            
            Responde de forma clara y amigable basándote en el contexto proporcionado.
        """.trimIndent()
            
            logger.debug("Enviando prompt a Gemini (${fullPrompt.length} caracteres)")
            val result = geminiService.generateContent(fullPrompt)
            
            result.onSuccess {
                logger.debug("Respuesta exitosa de Gemini (${it.length} caracteres)")
            }.onFailure { error ->
                logger.error("Error al generar contenido con Gemini", error)
            }
            
            return result
        } catch (e: Exception) {
            logger.error("Excepción no controlada en processMessage", e)
            return Result.failure(e)
        }
    }
}

