package com.siga.backend.repository

import com.siga.backend.entity.UsuarioSaas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioSaasRepository : JpaRepository<UsuarioSaas, Int> {
    fun findByEmail(email: String): Optional<UsuarioSaas>
    fun existsByEmail(email: String): Boolean
    fun findByUsuarioComercialId(usuarioComercialId: Int): List<UsuarioSaas>
}

