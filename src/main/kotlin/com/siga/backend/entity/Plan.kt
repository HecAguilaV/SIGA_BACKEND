package com.siga.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "PLANES", schema = "siga_comercial")
data class Plan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    
    @Column(nullable = false, unique = true, length = 100)
    val nombre: String,
    
    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,
    
    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    val precioMensual: BigDecimal,
    
    @Column(name = "precio_anual", precision = 10, scale = 2)
    val precioAnual: BigDecimal? = null,
    
    @Column(name = "limite_bodegas")
    val limiteBodegas: Int? = null,  // NULL = ilimitado
    
    @Column(name = "limite_usuarios")
    val limiteUsuarios: Int? = null,  // NULL = ilimitado
    
    @Column(name = "limite_productos")
    val limiteProductos: Int? = null,
    
    @Column(columnDefinition = "TEXT")
    val caracteristicas: String? = null, // JSON almacenado como texto
    
    @Column(nullable = false)
    val activo: Boolean = true,
    
    @Column(nullable = false)
    val orden: Int = 0,
    
    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: Instant = Instant.now()
)

