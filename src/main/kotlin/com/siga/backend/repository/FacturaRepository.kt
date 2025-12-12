package com.siga.backend.repository

import com.siga.backend.entity.Factura
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FacturaRepository : JpaRepository<Factura, Int> {
    fun findByUsuarioId(usuarioId: Int): List<Factura>
    fun findByNumeroFactura(numeroFactura: String): Factura?
    fun existsByNumeroFactura(numeroFactura: String): Boolean
}
