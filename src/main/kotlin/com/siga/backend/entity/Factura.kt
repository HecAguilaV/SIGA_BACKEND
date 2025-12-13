package com.siga.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

enum class EstadoFactura {
    PENDIENTE,
    PAGADA,
    VENCIDA,
    ANULADA
}

@Entity
@Table(name = "FACTURAS", schema = "siga_comercial")
data class Factura(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(name = "numero_factura", nullable = false, unique = true, length = 50)
    val numeroFactura: String,
    
    @Column(name = "usuario_id", nullable = false)
    val usuarioId: Int,
    
    @Column(name = "usuario_nombre", nullable = false, length = 255)
    val usuarioNombre: String,  // Denormalizado
    
    @Column(name = "usuario_email", nullable = false, length = 255)
    val usuarioEmail: String,  // Denormalizado
    
    @Column(name = "plan_id", nullable = false)
    val planId: Int,
    
    @Column(name = "plan_nombre", nullable = false, length = 255)
    val planNombre: String,  // Denormalizado
    
    @Column(name = "precio_uf", nullable = false, precision = 10, scale = 2)
    val precioUF: BigDecimal,
    
    @Column(name = "precio_clp", precision = 12, scale = 2)
    val precioCLP: BigDecimal? = null,
    
    @Column(nullable = false, length = 10)
    val unidad: String = "UF",
    
    @Column(name = "fecha_compra", nullable = false)
    val fechaCompra: Instant,
    
    @Column(name = "fecha_vencimiento")
    val fechaVencimiento: Instant? = null,
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val estado: EstadoFactura = EstadoFactura.PAGADA,
    
    @Column(name = "metodo_pago", length = 100)
    val metodoPago: String? = null,
    
    @Column(name = "ultimos_4_digitos", length = 4)
    val ultimos4Digitos: String? = null,
    
    // Campos opcionales para mantener relación con suscripción/pago
    @Column(name = "suscripcion_id")
    val suscripcionId: Int? = null,
    
    @Column(name = "pago_id")
    val pagoId: Int? = null,
    
    @Column(precision = 10, scale = 2)
    val iva: BigDecimal? = null,  // Opcional para futuro
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)
