package com.siga.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.siga.backend.entity.Producto
import com.siga.backend.repository.ProductoRepository
import com.siga.backend.service.SubscriptionService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.siga.backend.utils.TestSecurityUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import java.time.Instant

@WebMvcTest(controllers = [ProductosController::class])
@AutoConfigureMockMvc(addFilters = false)
class ProductosControllerTest {
    
    @BeforeEach
    fun setUp() {
        TestSecurityUtils.setupSecurityContext(1, "test@example.com", "ADMINISTRADOR")
    }
    
    @AfterEach
    fun tearDown() {
        TestSecurityUtils.clearSecurityContext()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var productoRepository: ProductoRepository

    @MockBean
    private lateinit var subscriptionService: SubscriptionService

    @Test
    fun `test listarProductos - success`() {
        val productos = listOf(
            Producto(
                id = 1,
                nombre = "Producto 1",
                descripcion = "Descripción 1",
                precioUnitario = BigDecimal("1000.50"),
                activo = true,
                fechaCreacion = Instant.now(),
                fechaActualizacion = Instant.now()
            ),
            Producto(
                id = 2,
                nombre = "Producto 2",
                precioUnitario = BigDecimal("2000.00"),
                activo = true,
                fechaCreacion = Instant.now(),
                fechaActualizacion = Instant.now()
            )
        )

        // Mock SecurityUtils.getUserEmail() retornando un email
        // Nota: En un test real, necesitarías configurar el SecurityContext con más detalles
        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        whenever(productoRepository.findByActivoTrue()).thenReturn(productos)

        mockMvc.perform(get("/api/saas/productos"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.productos").isArray)
            .andExpect(jsonPath("$.total").value(2))
    }

    @Test
    fun `test obtenerProducto - success`() {
        val producto = Producto(
            id = 1,
            nombre = "Producto Test",
            descripcion = "Descripción",
            precioUnitario = BigDecimal("1000.50"),
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        whenever(productoRepository.findById(1)).thenReturn(java.util.Optional.of(producto))

        mockMvc.perform(get("/api/saas/productos/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.producto.nombre").value("Producto Test"))
            .andExpect(jsonPath("$.producto.precioUnitario").value("1000.50"))
    }

    @Test
    fun `test obtenerProducto - not found`() {
        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        whenever(productoRepository.findById(999)).thenReturn(java.util.Optional.empty())

        mockMvc.perform(get("/api/saas/productos/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Producto no encontrado"))
    }

    @Test
    fun `test crearProducto - success`() {
        val request = ProductoRequest(
            nombre = "Nuevo Producto",
            descripcion = "Descripción",
            precioUnitario = "1500.75"
        )

        val productoGuardado = Producto(
            id = 1,
            nombre = "Nuevo Producto",
            descripcion = "Descripción",
            precioUnitario = BigDecimal("1500.75"),
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        doReturn(productoGuardado).whenever(productoRepository).save(any())

        mockMvc.perform(
            post("/api/saas/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Producto creado exitosamente"))
            .andExpect(jsonPath("$.producto.nombre").value("Nuevo Producto"))
    }

    @Test
    fun `test crearProducto - forbidden for non-admin`() {
        TestSecurityUtils.setupSecurityContext(2, "operador@example.com", "OPERADOR")
        val request = ProductoRequest(
            nombre = "Nuevo Producto",
            precioUnitario = "1500.75"
        )

        mockMvc.perform(
            post("/api/saas/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Solo administradores pueden crear productos"))
    }

    @Test
    fun `test actualizarProducto - success`() {
        val request = ProductoRequest(
            nombre = "Producto Actualizado",
            descripcion = "Nueva descripción",
            precioUnitario = "2000.00"
        )

        val productoExistente = Producto(
            id = 1,
            nombre = "Producto Original",
            precioUnitario = BigDecimal("1000.00"),
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        val productoActualizado = productoExistente.copy(
            nombre = "Producto Actualizado",
            descripcion = "Nueva descripción",
            precioUnitario = BigDecimal("2000.00"),
            fechaActualizacion = Instant.now()
        )

        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        whenever(productoRepository.findById(1)).thenReturn(java.util.Optional.of(productoExistente))
        doReturn(productoActualizado).whenever(productoRepository).save(any())

        mockMvc.perform(
            put("/api/saas/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Producto actualizado exitosamente"))
            .andExpect(jsonPath("$.producto.nombre").value("Producto Actualizado"))
    }

    @Test
    fun `test eliminarProducto - success`() {
        val producto = Producto(
            id = 1,
            nombre = "Producto a Eliminar",
            precioUnitario = BigDecimal("1000.00"),
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        val productoEliminado = producto.copy(activo = false)

        whenever(subscriptionService.hasActiveSubscription(any())).thenReturn(true)
        whenever(productoRepository.findById(1)).thenReturn(java.util.Optional.of(producto))
        doReturn(productoEliminado).whenever(productoRepository).save(any())

        mockMvc.perform(delete("/api/saas/productos/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Producto eliminado exitosamente"))
    }

    @Test
    fun `test listarProductos - unauthorized without authentication`() {
        TestSecurityUtils.clearSecurityContext()
        mockMvc.perform(get("/api/saas/productos"))
            .andExpect(status().isUnauthorized)
    }
}

