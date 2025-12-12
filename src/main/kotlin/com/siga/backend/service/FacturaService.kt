package com.siga.backend.service

import com.siga.backend.entity.Factura
import com.siga.backend.entity.EstadoFactura
import com.siga.backend.repository.FacturaRepository
import com.siga.backend.repository.UsuarioComercialRepository
import com.siga.backend.repository.PlanRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class FacturaService(
    private val facturaRepository: FacturaRepository,
    private val usuarioComercialRepository: UsuarioComercialRepository,
    private val planRepository: PlanRepository
) {
    private val logger = LoggerFactory.getLogger(FacturaService::class.java)
    
    /**
     * Genera número de factura único en formato FAC-YYYYMMDD-XXXX
     * XXXX es un contador secuencial del día
     */
    fun generarNumeroFactura(): String {
        val fecha = LocalDate.now()
        val fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        
        // Buscar última factura del día
        val prefijo = "FAC-$fechaStr-"
        val facturasDelDia = facturaRepository.findAll()
            .filter { it.numeroFactura.startsWith(prefijo) }
        
        val siguienteNumero = if (facturasDelDia.isEmpty()) {
            1
        } else {
            val ultimoNumero = facturasDelDia
                .map { it.numeroFactura.substringAfterLast("-").toIntOrNull() ?: 0 }
                .maxOrNull() ?: 0
            ultimoNumero + 1
        }
        
        val numeroFormateado = String.format("%04d", siguienteNumero)
        return "$prefijo$numeroFormateado"
    }
    
    @Transactional
    fun crearFactura(
        usuarioId: Int,
        usuarioNombre: String,
        usuarioEmail: String,
        planId: Int,
        planNombre: String,
        precioUF: BigDecimal,
        precioCLP: BigDecimal?,
        unidad: String = "UF",
        fechaVencimiento: Instant? = null,
        metodoPago: String? = null,
        ultimos4Digitos: String? = null,
        suscripcionId: Int? = null,
        pagoId: Int? = null
    ): Factura {
        logger.debug("Creando factura para usuario $usuarioId, plan $planId")
        
        // Validar que usuario y plan existen
        val usuario = usuarioComercialRepository.findById(usuarioId)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: $usuarioId") }
        
        val plan = planRepository.findById(planId)
            .orElseThrow { IllegalArgumentException("Plan no encontrado: $planId") }
        
        // Generar número de factura único
        var numeroFactura = generarNumeroFactura()
        var intentos = 0
        while (facturaRepository.existsByNumeroFactura(numeroFactura) && intentos < 10) {
            numeroFactura = generarNumeroFactura()
            intentos++
        }
        
        if (intentos >= 10) {
            throw IllegalStateException("No se pudo generar número de factura único después de 10 intentos")
        }
        
        val factura = Factura(
            numeroFactura = numeroFactura,
            usuarioId = usuarioId,
            usuarioNombre = usuarioNombre,  // Denormalizado
            usuarioEmail = usuarioEmail,      // Denormalizado
            planId = planId,
            planNombre = planNombre,          // Denormalizado
            precioUF = precioUF,
            precioCLP = precioCLP,
            unidad = unidad,
            fechaCompra = Instant.now(),
            fechaVencimiento = fechaVencimiento,
            estado = EstadoFactura.pagada,
            metodoPago = metodoPago,
            ultimos4Digitos = ultimos4Digitos,
            suscripcionId = suscripcionId,
            pagoId = pagoId
        )
        
        val facturaGuardada = facturaRepository.save(factura)
        logger.info("Factura creada: ${facturaGuardada.numeroFactura} para usuario $usuarioId")
        
        return facturaGuardada
    }
    
    fun obtenerFacturasPorUsuario(usuarioId: Int): List<Factura> {
        return facturaRepository.findByUsuarioId(usuarioId)
    }
    
    fun obtenerFacturaPorId(id: Int): Factura? {
        return facturaRepository.findById(id).orElse(null)
    }
    
    fun obtenerFacturaPorNumero(numeroFactura: String): Factura? {
        return facturaRepository.findByNumeroFactura(numeroFactura)
    }
}
