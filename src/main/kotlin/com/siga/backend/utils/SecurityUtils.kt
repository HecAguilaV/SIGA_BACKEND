package com.siga.backend.utils

import com.siga.backend.config.ApplicationContextProvider
import com.siga.backend.service.PermisosService
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    
    fun getUserId(): Int? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name?.toIntOrNull()
    }
    
    fun getUserEmail(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val details = authentication?.details as? Map<*, *>
        val email = details?.get("email") as? String
        // Retornar null si el email está vacío (no solo null)
        return if (email.isNullOrBlank()) null else email
    }
    
    fun getUserRol(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val details = authentication?.details as? Map<*, *>
        return details?.get("rol") as? String
    }
    
    fun hasRole(requiredRol: String): Boolean {
        val userRol = getUserRol()
        return userRol == requiredRol
    }
    
    fun isAdmin(): Boolean {
        return hasRole("ADMINISTRADOR")
    }
    
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico
     * ADMINISTRADOR siempre tiene todos los permisos
     */
    fun tienePermiso(codigoPermiso: String): Boolean {
        // Si es ADMINISTRADOR, tiene todos los permisos automáticamente
        if (isAdmin()) {
            return true
        }
        
        val userId = getUserId() ?: return false
        return try {
            val permisosService = ApplicationContextProvider.getBean(PermisosService::class.java)
            permisosService.tienePermiso(userId, codigoPermiso)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Métodos de conveniencia para permisos comunes
     */
    fun puedeCrearProductos(): Boolean = tienePermiso("PRODUCTOS_CREAR")
    fun puedeActualizarProductos(): Boolean = tienePermiso("PRODUCTOS_ACTUALIZAR")
    fun puedeEliminarProductos(): Boolean = tienePermiso("PRODUCTOS_ELIMINAR")
    fun puedeVerProductos(): Boolean = tienePermiso("PRODUCTOS_VER")
    
    fun puedeActualizarStock(): Boolean = tienePermiso("STOCK_ACTUALIZAR")
    fun puedeVerStock(): Boolean = tienePermiso("STOCK_VER")
    
    fun puedeCrearVentas(): Boolean = tienePermiso("VENTAS_CREAR")
    fun puedeVerVentas(): Boolean = tienePermiso("VENTAS_VER")
    
    fun puedeCrearLocales(): Boolean = tienePermiso("LOCALES_CREAR")
    fun puedeActualizarLocales(): Boolean = tienePermiso("LOCALES_ACTUALIZAR")
    fun puedeEliminarLocales(): Boolean = tienePermiso("LOCALES_ELIMINAR")
    
    fun puedeCrearCategorias(): Boolean = tienePermiso("CATEGORIAS_CREAR")
    fun puedeActualizarCategorias(): Boolean = tienePermiso("CATEGORIAS_ACTUALIZAR")
    fun puedeEliminarCategorias(): Boolean = tienePermiso("CATEGORIAS_ELIMINAR")
    
    fun puedeCrearUsuarios(): Boolean = tienePermiso("USUARIOS_CREAR")
    fun puedeAsignarPermisos(): Boolean = tienePermiso("USUARIOS_PERMISOS")
    
    fun puedeVerReportes(): Boolean = tienePermiso("REPORTES_VER")
    fun puedeVerCostos(): Boolean = tienePermiso("COSTOS_VER")
    
    fun puedeUsarAsistente(): Boolean = tienePermiso("ASISTENTE_USAR")
    fun puedeAnalisisIA(): Boolean = tienePermiso("ANALISIS_IA")
    fun puedeCRUDporIA(): Boolean = tienePermiso("ASISTENTE_CRUD")
    
    /**
     * Obtiene el usuario_comercial_id del usuario operativo actual
     * Retorna null si no se puede determinar
     */
    fun getUsuarioComercialId(): Int? {
        val logger = org.slf4j.LoggerFactory.getLogger(SecurityUtils::class.java)
        return try {
            val userId = getUserId()
            if (userId == null) {
                logger.warn("getUsuarioComercialId: userId es null")
                return null
            }
            
            val usuarioSaasRepository = ApplicationContextProvider.getBean(com.siga.backend.repository.UsuarioSaasRepository::class.java)
            val usuario = usuarioSaasRepository.findById(userId).orElse(null)
            if (usuario == null) {
                logger.warn("getUsuarioComercialId: usuario operativo no encontrado para userId=$userId")
                return null
            }
            
            // Si tiene usuario_comercial_id asignado, retornarlo
            if (usuario.usuarioComercialId != null) {
                logger.debug("getUsuarioComercialId: encontrado en usuario operativo: ${usuario.usuarioComercialId}")
                return usuario.usuarioComercialId
            }
            
            // Si no tiene, buscar por email en usuarios comerciales
            val email = getUserEmail()
            if (email == null) {
                logger.warn("getUsuarioComercialId: email es null para userId=$userId")
                return null
            }
            
            val usuarioComercialRepository = ApplicationContextProvider.getBean(com.siga.backend.repository.UsuarioComercialRepository::class.java)
            val usuarioComercial = usuarioComercialRepository.findByEmail(email.lowercase()).orElse(null)
            
            if (usuarioComercial != null) {
                // Actualizar el usuario operativo con el usuario_comercial_id encontrado
                val usuarioActualizado = usuario.copy(usuarioComercialId = usuarioComercial.id)
                usuarioSaasRepository.save(usuarioActualizado)
                logger.info("getUsuarioComercialId: actualizado usuario operativo $userId con usuario_comercial_id=${usuarioComercial.id}")
                return usuarioComercial.id
            }
            
            logger.warn("getUsuarioComercialId: no se encontró usuario comercial para email=$email")
            null
        } catch (e: Exception) {
            logger.error("Error al obtener usuario_comercial_id", e)
            null
        }
    }
}

