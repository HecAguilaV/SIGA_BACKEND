package com.siga.backend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controlador para redirigir rutas alternativas de Swagger UI
 */
@Controller
class SwaggerRedirectController {
    
    /**
     * Redirige /swagger-ui.html a /swagger-ui/index.html
     */
    @GetMapping("/swagger-ui.html")
    fun redirectToSwaggerUI(): String {
        return "redirect:/swagger-ui/index.html"
    }
    
    /**
     * Redirige /swagger-ui/ a /swagger-ui/index.html
     */
    @GetMapping("/swagger-ui")
    fun redirectToSwaggerUIRoot(): String {
        return "redirect:/swagger-ui/index.html"
    }
}

