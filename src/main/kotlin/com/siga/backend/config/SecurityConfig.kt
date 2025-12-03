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
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${cors.allowed-origins}") private val allowedOrigins: String
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
        configuration.allowedOrigins = allowedOrigins.split(",").map { it.trim() }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
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
                        "/api/comercial/chat",
                        "/api/comercial/planes/**",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/openapi.yaml"
                    ).permitAll()
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

