package com.siga.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.siga.backend.entity.Rol
import com.siga.backend.entity.UsuarioSaas
import com.siga.backend.repository.UsuarioSaasRepository
import com.siga.backend.service.JWTService
import com.siga.backend.service.PasswordService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

@WebMvcTest(controllers = [AuthController::class])
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var usuarioSaasRepository: UsuarioSaasRepository

    @MockBean
    private lateinit var passwordService: PasswordService

    @MockBean
    private lateinit var jwtService: JWTService

    @Test
    fun `test register - success`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            nombre = "Test",
            apellido = "User",
            rol = "OPERADOR"
        )

        val hashedPassword = "hashed_password"
        val savedUser = UsuarioSaas(
            id = 1,
            email = "test@example.com",
            passwordHash = hashedPassword,
            nombre = "Test",
            apellido = "User",
            rol = Rol.OPERADOR,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        whenever(usuarioSaasRepository.existsByEmail("test@example.com".lowercase())).thenReturn(false)
        whenever(passwordService.hashPassword("password123")).thenReturn(hashedPassword)
        doReturn(savedUser).whenever(usuarioSaasRepository).save(any())
        whenever(jwtService.generateAccessToken(1, "test@example.com", "OPERADOR")).thenReturn("access_token")
        whenever(jwtService.generateRefreshToken(1)).thenReturn("refresh_token")

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    @Test
    fun `test register - email already exists`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "password123",
            nombre = "Test",
            rol = "OPERADOR"
        )

        whenever(usuarioSaasRepository.existsByEmail("existing@example.com".lowercase())).thenReturn(true)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("El email ya está registrado"))
    }

    @Test
    fun `test login - success`() {
        val request = LoginRequest(
            email = "login@example.com",
            password = "password123"
        )

        val user = UsuarioSaas(
            id = 1,
            email = "login@example.com",
            passwordHash = "hashed_password",
            nombre = "Login",
            rol = Rol.OPERADOR,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        whenever(usuarioSaasRepository.findByEmail("login@example.com".lowercase())).thenReturn(java.util.Optional.of(user))
        whenever(passwordService.verifyPassword("password123", "hashed_password")).thenReturn(true)
        whenever(jwtService.generateAccessToken(1, "login@example.com", "OPERADOR")).thenReturn("access_token")
        whenever(jwtService.generateRefreshToken(1)).thenReturn("refresh_token")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.user.email").value("login@example.com"))
    }

    @Test
    fun `test login - invalid credentials`() {
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "wrongpassword"
        )

        whenever(usuarioSaasRepository.findByEmail("nonexistent@example.com".lowercase()))
            .thenReturn(java.util.Optional.empty())

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
    }

    @Test
    fun `test login - wrong password`() {
        val request = LoginRequest(
            email = "user@example.com",
            password = "wrongpassword"
        )

        val user = UsuarioSaas(
            id = 1,
            email = "user@example.com",
            passwordHash = "hashed_password",
            nombre = "User",
            rol = Rol.OPERADOR,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        whenever(usuarioSaasRepository.findByEmail("user@example.com".lowercase())).thenReturn(java.util.Optional.of(user))
        whenever(passwordService.verifyPassword("wrongpassword", "hashed_password")).thenReturn(false)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
    }

    @Test
    fun `test refresh token - success`() {
        val request = RefreshTokenRequest(refreshToken = "valid_refresh_token")

        val user = UsuarioSaas(
            id = 1,
            email = "user@example.com",
            passwordHash = "hashed_password",
            nombre = "User",
            rol = Rol.OPERADOR,
            activo = true,
            fechaCreacion = Instant.now(),
            fechaActualizacion = Instant.now()
        )

        val mockClaim = mock<com.auth0.jwt.interfaces.Claim>()
        whenever(mockClaim.asString()).thenReturn("refresh")
        
        val mockDecodedJWT = mock<com.auth0.jwt.interfaces.DecodedJWT>()
        whenever(mockDecodedJWT.getClaim("type")).thenReturn(mockClaim)
        whenever(mockDecodedJWT.subject).thenReturn("1")

        whenever(jwtService.verifyToken("valid_refresh_token")).thenReturn(mockDecodedJWT)
        whenever(usuarioSaasRepository.findById(1)).thenReturn(java.util.Optional.of(user))
        whenever(jwtService.generateAccessToken(1, "user@example.com", "OPERADOR")).thenReturn("new_access_token")
        whenever(jwtService.generateRefreshToken(1)).thenReturn("new_refresh_token")

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `test refresh token - invalid token`() {
        val request = RefreshTokenRequest(refreshToken = "invalid_token")

        whenever(jwtService.verifyToken("invalid_token")).thenReturn(null)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Token inválido"))
    }
}

