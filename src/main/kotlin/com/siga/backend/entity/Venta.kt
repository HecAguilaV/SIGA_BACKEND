package com.siga.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

enum class EstadoVenta {
    COMPLETADA,
    CANCELADA,
    PENDIENTE
}

@Entity
@Table(name = "VENTAS", schema = "siga_saas")
data class Venta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "local_id", nullable = false)
    val localId: Int,
    
    @Column(name = "usuario_id")
    val usuarioId: Int? = null,
    
    @Column(nullable = false)
    val fecha: Instant = Instant.now(),
    
    @Column(nullable = false, precision = 10, scale = 2)
    val total: BigDecimal,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val estado: EstadoVenta = EstadoVenta.COMPLETADA,
    
    @Column(columnDefinition = "TEXT")
    val observaciones: String? = null
)

@Entity
@Table(name = "DETALLES_VENTA", schema = "siga_saas")
data class DetalleVenta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "venta_id", nullable = false)
    val ventaId: Int,
    
    @Column(name = "producto_id", nullable = false)
    val productoId: Int,
    
    @Column(nullable = false)
    val cantidad: Int,
    
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    val precioUnitario: BigDecimal,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val subtotal: BigDecimal
)

