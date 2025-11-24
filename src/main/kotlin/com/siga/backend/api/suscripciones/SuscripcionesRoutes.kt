package com.siga.backend.api.suscripciones

import com.siga.backend.config.*
import com.siga.backend.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

@Serializable
data class SuscripcionRequest(
    val planId: Int,
    val periodo: String = "MENSUAL" // MENSUAL o ANUAL
)

@Serializable
data class SuscripcionResponse(
    val id: Int,
    val usuarioId: Int,
    val planId: Int,
    val fechaInicio: String,
    val fechaFin: String?,
    val estado: String,
    val periodo: String
)

@Serializable
data class SuscripcionesListResponse(
    val success: Boolean,
    val suscripciones: List<SuscripcionResponse>,
    val total: Int
)

@Serializable
data class SuscripcionDetailResponse(
    val success: Boolean,
    val suscripcion: SuscripcionResponse? = null,
    val message: String? = null
)

/**
 * Rutas para Suscripciones
 * Requiere autenticación JWT
 */
fun Application.configureSuscripcionesRoutes() {
    routing {
        route("/api/comercial/suscripciones") {
            authenticate("jwt") {
                // GET /api/comercial/suscripciones - Listar suscripciones del usuario autenticado
                get {
                    try {
                        val userId = call.getUserId()
                            ?: throw IllegalStateException("Usuario no autenticado")
                        
                        // Obtener email del usuario autenticado
                        val userEmail = call.getUserEmail()
                            ?: throw IllegalStateException("Email no disponible")
                        
                        // Buscar usuario en siga_comercial por email
                        val usuarioComercialId = transaction {
                            UsuarioComercialTable.select {
                                UsuarioComercialTable.email eq userEmail
                            }.firstOrNull()?.get(UsuarioComercialTable.id)
                        }
                        
                        if (usuarioComercialId == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                SuscripcionesListResponse(success = false, suscripciones = emptyList(), total = 0)
                            )
                            return@get
                        }
                        
                        val suscripciones = transaction {
                            SuscripcionTable.select {
                                SuscripcionTable.usuarioId eq usuarioComercialId
                            }.orderBy(SuscripcionTable.fechaCreacion, SortOrder.DESC).map { row ->
                                SuscripcionResponse(
                                    id = row[SuscripcionTable.id],
                                    usuarioId = row[SuscripcionTable.usuarioId],
                                    planId = row[SuscripcionTable.planId],
                                    fechaInicio = row[SuscripcionTable.fechaInicio].toString(),
                                    fechaFin = row[SuscripcionTable.fechaFin]?.toString(),
                                    estado = row[SuscripcionTable.estado],
                                    periodo = row[SuscripcionTable.periodo]
                                )
                            }
                        }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            SuscripcionesListResponse(success = true, suscripciones = suscripciones, total = suscripciones.size)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            SuscripcionesListResponse(success = false, suscripciones = emptyList(), total = 0)
                        )
                    }
                }
                
                // POST /api/comercial/suscripciones - Crear suscripción
                post {
                    try {
                        val userId = call.getUserId()
                            ?: throw IllegalStateException("Usuario no autenticado")
                        
                        val userEmail = call.getUserEmail()
                            ?: throw IllegalStateException("Email no disponible")
                        
                        val request = call.receive<SuscripcionRequest>()
                        
                        // Validar periodo
                        if (request.periodo !in listOf("MENSUAL", "ANUAL")) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                SuscripcionDetailResponse(success = false, message = "Periodo inválido. Debe ser MENSUAL o ANUAL")
                            )
                            return@post
                        }
                        
                        // Buscar usuario en siga_comercial
                        val usuarioComercialId = transaction {
                            UsuarioComercialTable.select {
                                UsuarioComercialTable.email eq userEmail
                            }.firstOrNull()?.get(UsuarioComercialTable.id)
                        }
                        
                        if (usuarioComercialId == null) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                SuscripcionDetailResponse(success = false, message = "Usuario comercial no encontrado")
                            )
                            return@post
                        }
                        
                        // Calcular fechas
                        val fechaInicio = LocalDate.now()
                        val fechaFin = when (request.periodo) {
                            "MENSUAL" -> fechaInicio.plusMonths(1)
                            "ANUAL" -> fechaInicio.plusYears(1)
                            else -> fechaInicio.plusMonths(1)
                        }
                        
                        val suscripcionId = transaction {
                            SuscripcionTable.insert {
                                it[SuscripcionTable.usuarioId] = usuarioComercialId
                                it[SuscripcionTable.planId] = request.planId
                                it[SuscripcionTable.fechaInicio] = fechaInicio
                                it[SuscripcionTable.fechaFin] = fechaFin
                                it[SuscripcionTable.estado] = "ACTIVA"
                                it[SuscripcionTable.periodo] = request.periodo
                            }
                            
                            // Obtener el ID de la suscripción creada
                            SuscripcionTable.select {
                                (SuscripcionTable.usuarioId eq usuarioComercialId) and
                                (SuscripcionTable.planId eq request.planId)
                            }.orderBy(SuscripcionTable.fechaCreacion, SortOrder.DESC).first()[SuscripcionTable.id]
                        }
                        
                        // Obtener la suscripción creada
                        val suscripcion = transaction {
                            SuscripcionTable.select {
                                SuscripcionTable.id eq suscripcionId
                            }.first()
                        }
                        
                        val suscripcionResponse = SuscripcionResponse(
                            id = suscripcion[SuscripcionTable.id],
                            usuarioId = suscripcion[SuscripcionTable.usuarioId],
                            planId = suscripcion[SuscripcionTable.planId],
                            fechaInicio = suscripcion[SuscripcionTable.fechaInicio].toString(),
                            fechaFin = suscripcion[SuscripcionTable.fechaFin]?.toString(),
                            estado = suscripcion[SuscripcionTable.estado],
                            periodo = suscripcion[SuscripcionTable.periodo]
                        )
                        
                        call.respond(
                            HttpStatusCode.Created,
                            SuscripcionDetailResponse(success = true, suscripcion = suscripcionResponse)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            SuscripcionDetailResponse(success = false, message = "Error al crear suscripción: ${e.message}")
                        )
                    }
                }
            }
        }
    }
}

