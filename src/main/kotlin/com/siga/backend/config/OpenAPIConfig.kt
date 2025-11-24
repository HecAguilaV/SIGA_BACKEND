package com.siga.backend.config

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.io.InputStream

/**
 * Configuración de OpenAPI/Swagger para documentación de la API
 * 
 * Sirve el archivo YAML de OpenAPI para ser usado con Swagger UI externo
 * o herramientas como Postman, Insomnia, etc.
 */
fun Application.configureOpenAPI() {
    routing {
        get("/openapi.yaml") {
            val yamlStream: InputStream? = 
                this::class.java.classLoader.getResourceAsStream("openapi/documentation.yaml")
            
            if (yamlStream != null) {
                val yamlContent = yamlStream.bufferedReader().use { it.readText() }
                call.respondText(yamlContent, ContentType("application", "yaml"))
            } else {
                call.respond(HttpStatusCode.NotFound, "OpenAPI documentation not found")
            }
        }
        
        get("/api-docs") {
            call.respondText(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>SIGA API Documentation</title>
                    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css" />
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-bundle.js"></script>
                    <script>
                        SwaggerUIBundle({
                            url: '/openapi.yaml',
                            dom_id: '#swagger-ui',
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIBundle.presets.standalone
                            ]
                        });
                    </script>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
        }
    }
}

