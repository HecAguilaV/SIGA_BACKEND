package com.siga.backend.service

import com.siga.backend.entity.Permiso
import com.siga.backend.entity.Rol
import com.siga.backend.entity.RolPermiso
import com.siga.backend.entity.RolPermisoId
import com.siga.backend.entity.UsuarioPermiso
import com.siga.backend.entity.UsuarioPermisoId
import com.siga.backend.repository.PermisoRepository
import com.siga.backend.repository.RolPermisoRepository
import com.siga.backend.repository.UsuarioPermisoRepository
import com.siga.backend.repository.UsuarioSaasRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PermisosService(
    private val permisosRepository: PermisoRepository,
    private val rolesPermisosRepository: RolPermisoRepository,
    private val usuariosPermisosRepository: UsuarioPermisoRepository,
    private val usuarioSaasRepository: UsuarioSaasRepository
) {
    
    /**
     * Verifica si un usuario tiene un permiso específico
     * Admin siempre tiene todos los permisos
     */
    fun tienePermiso(usuarioId: Int, codigoPermiso: String): Boolean {
        val usuario = usuarioSaasRepository.findById(usuarioId).orElse(null)
            ?: return false
        
        // Admin siempre tiene todos los permisos
        if (usuario.rol == Rol.ADMINISTRADOR) {
            return true
        }
        
        val permiso = permisosRepository.findByCodigo(codigoPermiso).orElse(null)
            ?: return false
        
        // Verificar si el permiso está en el rol base
        val tienePorRol = rolesPermisosRepository.existsById_RolAndId_PermisoId(
            usuario.rol.name,
            permiso.id
        )
        
        // Verificar si tiene permiso adicional asignado
        val tieneAdicional = usuariosPermisosRepository.existsById_UsuarioIdAndId_PermisoId(
            usuarioId,
            permiso.id
        )
        
        return tienePorRol || tieneAdicional
    }
    
    /**
     * Obtiene todos los permisos de un usuario (rol + adicionales)
     */
    fun obtenerPermisosUsuario(usuarioId: Int): List<String> {
        val usuario = usuarioSaasRepository.findById(usuarioId).orElse(null)
            ?: return emptyList()
        
        // Admin tiene todos los permisos
        if (usuario.rol == Rol.ADMINISTRADOR) {
            return permisosRepository.findByActivoTrue().map { it.codigo }
        }
        
        // Permisos del rol
        val permisosRol = rolesPermisosRepository
            .findById_Rol(usuario.rol.name)
            .mapNotNull { it.permiso?.codigo }
        
        // Permisos adicionales
        val permisosAdicionales = usuariosPermisosRepository
            .findByUsuarioId(usuarioId)
            .mapNotNull { it.permiso?.codigo }
        
        return (permisosRol + permisosAdicionales).distinct()
    }
    
    /**
     * Asigna un permiso adicional a un usuario (solo admin puede hacerlo)
     */
    @Transactional
    fun asignarPermiso(usuarioId: Int, codigoPermiso: String, asignadoPor: Int): Boolean {
        val permiso = permisosRepository.findByCodigo(codigoPermiso).orElse(null)
            ?: return false
        
        val usuario = usuarioSaasRepository.findById(usuarioId).orElse(null)
            ?: return false
        
        // No asignar permisos a admin (ya los tiene todos)
        if (usuario.rol == Rol.ADMINISTRADOR) {
            return true
        }
        
        // Verificar si ya tiene el permiso (por rol o adicional)
        if (tienePermiso(usuarioId, codigoPermiso)) {
            // Si ya lo tiene por rol, no es necesario asignarlo adicionalmente
            val tienePorRol = rolesPermisosRepository.existsById_RolAndId_PermisoId(
                usuario.rol.name,
                permiso.id
            )
            if (tienePorRol) {
                return true // Ya lo tiene por rol
            }
            // Si ya lo tiene como adicional, no hacer nada
            return true
        }
        
        // Crear permiso adicional
        val usuarioPermiso = UsuarioPermiso(
            id = UsuarioPermisoId(
                usuarioId = usuarioId,
                permisoId = permiso.id
            ),
            asignadoPor = asignadoPor
        )
        
        usuariosPermisosRepository.save(usuarioPermiso)
        return true
    }
    
    /**
     * Revoca un permiso adicional de un usuario (solo admin puede hacerlo)
     */
    @Transactional
    fun revocarPermiso(usuarioId: Int, codigoPermiso: String): Boolean {
        val permiso = permisosRepository.findByCodigo(codigoPermiso).orElse(null)
            ?: return false
        
        val usuario = usuarioSaasRepository.findById(usuarioId).orElse(null)
            ?: return false
        
        // No revocar permisos de admin
        if (usuario.rol == Rol.ADMINISTRADOR) {
            return false
        }
        
        // Solo revocar si es un permiso adicional (no los del rol)
        val tienePorRol = rolesPermisosRepository.existsById_RolAndId_PermisoId(
            usuario.rol.name,
            permiso.id
        )
        
        if (tienePorRol) {
            return false // No se puede revocar un permiso del rol base
        }
        
        usuariosPermisosRepository.deleteById(
            UsuarioPermisoId(
                usuarioId = usuarioId,
                permisoId = permiso.id
            )
        )
        return true
    }
    
    /**
     * Obtiene todos los permisos disponibles del sistema
     */
    fun obtenerTodosPermisos(): List<Permiso> {
        return permisosRepository.findByActivoTrue()
    }
    
    /**
     * Obtiene permisos por categoría
     */
    fun obtenerPermisosPorCategoria(categoria: String): List<Permiso> {
        return permisosRepository.findByCategoria(categoria)
    }
    
    /**
     * Obtiene permisos de un usuario agrupados por categoría
     */
    fun obtenerPermisosUsuarioPorCategoria(usuarioId: Int): Map<String, List<String>> {
        val permisos = obtenerPermisosUsuario(usuarioId)
        val permisosCompletos = permisosRepository.findByCodigoIn(permisos)
        
        return permisosCompletos.groupBy { it.categoria }
            .mapValues { (_, permisos) -> permisos.map { it.codigo } }
    }
}
