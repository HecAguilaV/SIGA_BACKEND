package com.siga.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${jwt.secret:default_secret_change_in_production}") private val jwtSecret: String,
    @Value("\${cors.allowed-origins:*}") private val allowedOrigins: String
) {
    
    @Bean
    fun jwtAlgorithm(): Algorithm {
        return Algorithm.HMAC256(jwtSecret)
    }
    
    @Bean
    fun jwtVerifier(): com.auth0.jwt.interfaces.JWTVerifier {
        return JWT.require(jwtAlgorithm())
            .withIssuer("siga-backend")
            .build()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // Detectar si estamos en desarrollo (no en Railway/producción)
        val isDevelopment = System.getenv("RAILWAY_ENVIRONMENT") == null && 
                           System.getenv("RAILWAY_PUBLIC_DOMAIN") == null
        
        if (isDevelopment) {
            // En desarrollo: permitir cualquier puerto de localhost usando patrones
            configuration.allowedOriginPatterns = listOf(
                "http://localhost:*",
                "http://127.0.0.1:*"
            )
        } else {
            // En producción: usar orígenes específicos de la configuración
            val origins = allowedOrigins.split(",").map { it.trim() }
            configuration.allowedOrigins = origins
        }
        
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        
        // Configuración especial para Swagger/OpenAPI (permite cualquier origen)
        val swaggerConfig = CorsConfiguration()
        swaggerConfig.allowedOriginPatterns = listOf("*")
        swaggerConfig.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
        swaggerConfig.allowedHeaders = listOf("*")
        swaggerConfig.allowCredentials = false
        swaggerConfig.maxAge = 3600L
        
        source.registerCorsConfiguration("/swagger-ui/**", swaggerConfig)
        source.registerCorsConfiguration("/api-docs/**", swaggerConfig)
        source.registerCorsConfiguration("/v3/api-docs/**", swaggerConfig)
        source.registerCorsConfiguration("/openapi.yaml", swaggerConfig)
        source.registerCorsConfiguration("/openapi.yml", swaggerConfig)
        
        // Configuración normal para el resto de endpoints
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoints públicos
                    .requestMatchers(
                        "/health",
                        "/api/auth/**",
                        "/api/comercial/auth/**",
                        "/api/comercial/chat",
                        "/api/comercial/planes/**",
                        "/swagger-ui/**",
                        "/swagger-ui",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/api-docs.yaml",
                        "/api-docs.yml",
                        "/openapi.yaml",
                        "/openapi.yml"
                    ).permitAll()
                    // Endpoints administrativos requieren autenticación
                    .requestMatchers("/api/admin/**").authenticated()
                    // Todos los demás requieren autenticación
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtVerifier()),
                UsernamePasswordAuthenticationFilter::class.java
            )
        
        return http.build()
    }
}

