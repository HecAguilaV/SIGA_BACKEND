package com.siga.backend.controller

import com.siga.backend.service.PermisosService
import com.siga.backend.utils.SecurityUtils
import com.siga.backend.repository.UsuarioSaasRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/saas/debug")
class DebugController(
    private val permisosService: PermisosService,
    private val usuarioSaasRepository: UsuarioSaasRepository
) {

    @GetMapping("/info")
    fun debugInfo(): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtils.getUserId()
        val email = SecurityUtils.getUserEmail()
        val rol = SecurityUtils.getUserRol()
        val usuarioComercialId = SecurityUtils.getUsuarioComercialId()

        val dbUser = if (userId != null) {
            usuarioSaasRepository.findById(userId).orElse(null)
        } else null

        val permisos = if (userId != null) {
            permisosService.obtenerPermisosUsuario(userId)
        } else emptyList()

        val checkProductos = SecurityUtils.tienePermiso("PRODUCTOS_VER")
        val checkStock = SecurityUtils.tienePermiso("STOCK_VER")
        
        return ResponseEntity.ok(mapOf(
            "auth_context" to mapOf(
                "userId" to userId,
                "email" to email,
                "rol" to rol,
                "usuarioComercialId" to usuarioComercialId
            ),
            "db_user" to mapOf(
                "id" to dbUser?.id,
                "rol_real" to dbUser?.rol,
                "email_real" to dbUser?.email
            ),
            "permisos_calculados" to permisos,
            "checks_explicit" to mapOf(
                "PRODUCTOS_VER" to checkProductos,
                "STOCK_VER" to checkStock
            )
        ))
    }
}
