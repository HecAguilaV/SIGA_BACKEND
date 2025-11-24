package com.siga.backend.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

enum class EstadoVenta {
    COMPLETADA,
    CANCELADA,
    PENDIENTE
}

object VentaTable : Table("siga_saas.VENTAS") {
    
    val id = integer("id").autoIncrement()
    val localId = integer("local_id").references(LocalTable.id, onDelete = ReferenceOption.CASCADE)
    val usuarioId = integer("usuario_id").nullable()
    val fecha = timestamp("fecha").default(Instant.now())
    val total = decimal("total", 10, 2).check { it greaterEq BigDecimal.ZERO }
    val estado = varchar("estado", 20).default("COMPLETADA")
    val observaciones = text("observaciones").nullable()
}

object DetalleVentaTable : Table("siga_saas.DETALLES_VENTA") {
    
    val id = integer("id").autoIncrement()
    val ventaId = integer("venta_id").references(VentaTable.id, onDelete = ReferenceOption.CASCADE)
    val productoId = integer("producto_id").references(ProductoTable.id, onDelete = ReferenceOption.CASCADE)
    val cantidad = integer("cantidad").check { it greater 0 }
    val precioUnitario = decimal("precio_unitario", 10, 2).check { it greaterEq BigDecimal.ZERO }
    val subtotal = decimal("subtotal", 10, 2).check { it greaterEq BigDecimal.ZERO }
}
