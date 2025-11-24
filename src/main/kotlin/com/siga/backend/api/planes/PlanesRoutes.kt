package com.siga.backend.api.planes

import com.siga.backend.models.PlanTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class PlanResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precioMensual: String,
    val precioAnual: String?,
    val limiteBodegas: Int,
    val limiteUsuarios: Int,
    val limiteProductos: Int?,
    val activo: Boolean
)

@Serializable
data class PlanesListResponse(
    val success: Boolean,
    val planes: List<PlanResponse>,
    val total: Int
)

@Serializable
data class PlanDetailResponse(
    val success: Boolean,
    val plan: PlanResponse? = null,
    val message: String? = null
)

/**
 * Rutas para Planes (públicas - no requieren autenticación)
 */
fun Application.configurePlanesRoutes() {
    routing {
        route("/api/comercial/planes") {
            // GET /api/comercial/planes - Listar todos los planes activos
            get {
                try {
                    val planes = transaction {
                        PlanTable.select {
                            PlanTable.activo eq true
                        }.orderBy(PlanTable.orden).map { row ->
                            PlanResponse(
                                id = row[PlanTable.id],
                                nombre = row[PlanTable.nombre],
                                descripcion = row[PlanTable.descripcion],
                                precioMensual = row[PlanTable.precioMensual].toString(),
                                precioAnual = row[PlanTable.precioAnual]?.toString(),
                                limiteBodegas = row[PlanTable.limiteBodegas],
                                limiteUsuarios = row[PlanTable.limiteUsuarios],
                                limiteProductos = row[PlanTable.limiteProductos],
                                activo = row[PlanTable.activo]
                            )
                        }
                    }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        PlanesListResponse(success = true, planes = planes, total = planes.size)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        PlanesListResponse(success = false, planes = emptyList(), total = 0)
                    )
                }
            }
            
            // GET /api/comercial/planes/{id} - Obtener plan por ID
            get("{id}") {
                try {
                    val planId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("ID inválido")
                    
                    val plan = transaction {
                        PlanTable.select {
                            (PlanTable.id eq planId) and (PlanTable.activo eq true)
                        }.firstOrNull()
                    }
                    
                    if (plan == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            PlanDetailResponse(success = false, message = "Plan no encontrado")
                        )
                        return@get
                    }
                    
                    val planResponse = PlanResponse(
                        id = plan[PlanTable.id],
                        nombre = plan[PlanTable.nombre],
                        descripcion = plan[PlanTable.descripcion],
                        precioMensual = plan[PlanTable.precioMensual].toString(),
                        precioAnual = plan[PlanTable.precioAnual]?.toString(),
                        limiteBodegas = plan[PlanTable.limiteBodegas],
                        limiteUsuarios = plan[PlanTable.limiteUsuarios],
                        limiteProductos = plan[PlanTable.limiteProductos],
                        activo = plan[PlanTable.activo]
                    )
                    
                    call.respond(
                        HttpStatusCode.OK,
                        PlanDetailResponse(success = true, plan = planResponse)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        PlanDetailResponse(success = false, message = "Error: ${e.message}")
                    )
                }
            }
        }
    }
}

