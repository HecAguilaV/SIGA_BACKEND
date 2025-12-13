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
}

