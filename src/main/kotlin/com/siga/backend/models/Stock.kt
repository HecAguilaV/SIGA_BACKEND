package com.siga.backend.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object StockTable : Table("siga_saas.STOCK") {
    
    val id = integer("id").autoIncrement()
    val productoId = integer("producto_id").references(ProductoTable.id, onDelete = ReferenceOption.CASCADE)
    val localId = integer("local_id").references(LocalTable.id, onDelete = ReferenceOption.CASCADE)
    val cantidad = integer("cantidad").default(0).check { it greaterEq 0 }
    val cantidadMinima = integer("cantidad_minima").default(0).check { it greaterEq 0 }
    val fechaActualizacion = timestamp("fecha_actualizacion").default(Instant.now())
    
    init {
        uniqueIndex(productoId, localId)
    }
}
