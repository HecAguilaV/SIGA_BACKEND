package com.siga.backend.repository

import com.siga.backend.entity.Stock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface StockRepository : JpaRepository<Stock, Int> {
    @Query("SELECT s FROM Stock s WHERE s.productoId = :productoId AND s.localId = :localId")
    fun findByProductoIdAndLocalId(@Param("productoId") productoId: Int, @Param("localId") localId: Int): Optional<Stock>
    
    @Query("SELECT s FROM Stock s WHERE s.localId = :localId")
    fun findByLocalId(@Param("localId") localId: Int): List<Stock>
}

