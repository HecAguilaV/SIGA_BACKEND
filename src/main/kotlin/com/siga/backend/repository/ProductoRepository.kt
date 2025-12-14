package com.siga.backend.repository

import com.siga.backend.entity.Producto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductoRepository : JpaRepository<Producto, Int> {
    fun findByActivoTrue(): List<Producto>
    fun findByActivoTrueAndUsuarioComercialId(usuarioComercialId: Int): List<Producto>
    fun findByUsuarioComercialId(usuarioComercialId: Int): List<Producto>
}

