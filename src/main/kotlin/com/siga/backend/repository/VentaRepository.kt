package com.siga.backend.repository

import com.siga.backend.entity.Venta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VentaRepository : JpaRepository<Venta, Int> {
    fun findByUsuarioComercialId(usuarioComercialId: Int): List<Venta>
}

