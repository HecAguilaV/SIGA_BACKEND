package com.siga.backend.utils

import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.PermisosService
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    
    private var permisosService: PermisosService? = null
    private var usuarioSaasRepository: UsuarioSaasRepository? = null
    private var usuarioComercialRepository: UsuarioComercialRepository? = null
    
    fun init(
        permisosService: PermisosService,
        usuarioSaasRepository: UsuarioSaasRepository,
        usuarioComercialRepository: UsuarioComercialRepository
    ) {
        this.permisosService = permisosService
        this.usuarioSaasRepository = usuarioSaasRepository
        this.usuarioComercialRepository = usuarioComercialRepository
    }
    
    fun getUserId(): Int? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.name?.toIntOrNull()
    }
    
    fun getUserEmail(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val details = authentication?.details as? Map<*, *>
        val email = details?.get("email") as? String
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
    
    fun tienePermiso(codigoPermiso: String): Boolean {
        if (isAdmin()) return true
        
        val userId = getUserId() ?: return false
        val service = permisosService ?: run {
            org.slf4j.LoggerFactory.getLogger(SecurityUtils::class.java).error("SecurityUtils no inicializado: permisosService es null")
            return false
        }
        
        return try {
            service.tienePermiso(userId, codigoPermiso)
        } catch (e: Exception) {
            org.slf4j.LoggerFactory.getLogger(SecurityUtils::class.java).error("Error al verificar permiso $codigoPermiso para usuario $userId", e)
            false
        }
    }
    
    // Métodos de conveniencia
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
    fun puedeVerLocales(): Boolean = tienePermiso("LOCALES_VER")
    
    fun puedeCrearCategorias(): Boolean = tienePermiso("CATEGORIAS_CREAR")
    fun puedeActualizarCategorias(): Boolean = tienePermiso("CATEGORIAS_ACTUALIZAR")
    fun puedeEliminarCategorias(): Boolean = tienePermiso("CATEGORIAS_ELIMINAR")
    fun puedeVerCategorias(): Boolean = tienePermiso("CATEGORIAS_VER")
    
    fun puedeCrearUsuarios(): Boolean = tienePermiso("USUARIOS_CREAR")
    fun puedeAsignarPermisos(): Boolean = tienePermiso("USUARIOS_PERMISOS")
    
    fun puedeVerReportes(): Boolean = tienePermiso("REPORTES_VER")
    fun puedeVerCostos(): Boolean = tienePermiso("COSTOS_VER")
    
    fun puedeUsarAsistente(): Boolean = tienePermiso("ASISTENTE_USAR")
    fun puedeAnalisisIA(): Boolean = tienePermiso("ANALISIS_IA")
    fun puedeCRUDporIA(): Boolean = tienePermiso("ASISTENTE_CRUD")
    
    fun getUsuarioComercialId(): Int? {
        val logger = org.slf4j.LoggerFactory.getLogger(SecurityUtils::class.java)
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            val details = authentication?.details as? Map<*, *>
            val usuarioComercialIdFromToken = details?.get("usuario_comercial_id") as? Int
            if (usuarioComercialIdFromToken != null) {
                return usuarioComercialIdFromToken
            }
            
            val email = getUserEmail()
            val userId = getUserId()
            
            if (email != null) {
                if (userId != null) {
                    val repo = usuarioSaasRepository ?: return null
                    val usuario = repo.findById(userId).orElse(null)
                    if (usuario != null && usuario.usuarioComercialId != null) {
                        return usuario.usuarioComercialId
                    }
                }
                
                // Fallback email
                val repoComercial = usuarioComercialRepository ?: return null
                val usuarioComercial = repoComercial.findByEmail(email.lowercase()).orElse(null)
                if (usuarioComercial != null) {
                    // Actualizar usuario operativo si es posible
                    if (userId != null) {
                        try {
                            val repo = usuarioSaasRepository
                            if (repo != null) {
                                val usuario = repo.findById(userId).orElse(null)
                                if (usuario != null && usuario.usuarioComercialId == null) {
                                    val usuarioActualizado = usuario.copy(usuarioComercialId = usuarioComercial.id)
                                    repo.save(usuarioActualizado)
                                }
                            }
                        } catch (e: Exception) {
                            logger.warn("No se pudo actualizar usuario operativo", e)
                        }
                    }
                    return usuarioComercial.id
                }
            }
            null
        } catch (e: Exception) {
            logger.error("Error al obtener usuario_comercial_id", e)
            null
        }
    }
}

