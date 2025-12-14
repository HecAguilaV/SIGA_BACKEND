package com.siga.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "PRODUCTOS", schema = "siga_saas")
data class Producto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, length = 200)
    val nombre: String,
    
    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,
    
    @Column(name = "categoria_id")
    val categoriaId: Int? = null,
    
    @Column(name = "codigo_barras", length = 50, unique = true)
    val codigoBarras: String? = null,
    
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    val precioUnitario: BigDecimal? = null,
    
    @Column(name = "usuario_comercial_id")
    val usuarioComercialId: Int? = null,  // ID del usuario comercial (dueño) al que pertenece
    
    @Column(nullable = false)
    val activo: Boolean = true,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now(),
    
    @Column(name = "fecha_actualizacion", nullable = false)
    val fechaActualizacion: Instant = Instant.now()
)

