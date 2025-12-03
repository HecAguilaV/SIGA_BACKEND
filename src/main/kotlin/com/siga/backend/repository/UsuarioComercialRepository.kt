package com.siga.backend.repository

import com.siga.backend.entity.UsuarioComercial
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioComercialRepository : JpaRepository<UsuarioComercial, Int> {
    fun findByEmail(email: String): Optional<UsuarioComercial>
    fun existsByEmail(email: String): Boolean
}

