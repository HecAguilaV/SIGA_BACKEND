package com.siga.backend.config

import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.PermisosService
import com.siga.backend.utils.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class SecurityUtilsConfig(
    private val permisosService: PermisosService,
    private val usuarioSaasRepository: UsuarioSaasRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository
) {
    private val logger = LoggerFactory.getLogger(SecurityUtilsConfig::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        logger.info("SecurityUtilsConfig: INICIALIZANDO SecurityUtils...")
        try {
            SecurityUtils.init(permisosService, usuarioSaasRepository, usuarioComercialRepository)
            logger.info("SecurityUtilsConfig: SecurityUtils inicializado CORRECTAMENTE")
        } catch (e: Exception) {
            logger.error("SecurityUtilsConfig: ALERTA - Falló la inicialización de SecurityUtils", e)
        }
    }
}
