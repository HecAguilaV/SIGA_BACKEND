package com.siga.backend.repository

import com.siga.backend.entity.Local
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocalRepository : JpaRepository<Local, Int> {
    fun findByActivoTrue(): List<Local>
    fun findByActivoTrueAndUsuarioComercialId(usuarioComercialId: Int): List<Local>
    fun findByUsuarioComercialId(usuarioComercialId: Int): List<Local>
}
