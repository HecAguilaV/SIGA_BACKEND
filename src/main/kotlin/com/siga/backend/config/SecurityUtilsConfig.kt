package com.siga.backend.config

import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.PermisosService
import com.siga.backend.utils.SecurityUtils
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@DependsOn("applicationContextProvider") // Asegurar orden si fuera necesario, aunque aquí inyectaremos directo
class SecurityUtilsConfig(
    private val permisosService: PermisosService,
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository
) {

    @PostConstruct
    fun init() {
        SecurityUtils.init(permisosService, usuarioSaasRepository, usuarioComercialRepository)
    }
}
