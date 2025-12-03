package com.siga.backend.repository

import com.siga.backend.entity.DetalleVenta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DetalleVentaRepository : JpaRepository<DetalleVenta, Int>

