# Documentación SIGA Backend

Este directorio contiene toda la documentación técnica del backend de SIGA.

## Documentos Disponibles

### Documentación Técnica

1. **API_DOCUMENTACION.md**
   - Documentación completa de todos los endpoints
   - Especificaciones de request/response
   - Códigos de estado HTTP
   - Validaciones y reglas de negocio

2. **APIS_INTEGRACION.md**
   - Guía de integración para frontends
   - Ejemplos de código (JavaScript, TypeScript, Kotlin)
   - Clientes API reutilizables
   - Mejores prácticas de integración

3. **COBERTURA_TESTING.md**
   - Resumen de la suite de tests
   - Estadísticas de cobertura
   - Estrategia de testing
   - Guía de ejecución

### Documentación de APIs Frontend

4. **API_FRONTEND_APP.md**
   - Endpoints específicos para la app móvil Android

5. **API_FRONTEND_APPWEB.md**
   - Endpoints específicos para la aplicación web operativa

6. **API_FRONTEND_COMERCIAL.md**
   - Endpoints específicos para el portal comercial

## Documentación Interactiva

### Swagger UI
Documentación interactiva disponible en:
- Producción: `https://siga-backend-production.up.railway.app/swagger-ui/index.html`
- Local: `http://localhost:8080/swagger-ui/index.html`

### OpenAPI Spec
Especificación OpenAPI disponible en:
- JSON: `/api-docs`
- YAML: `/openapi.yaml`

## Estructura de Documentación

```
docs/
├── README.md (este archivo)
├── API_DOCUMENTACION.md
├── APIS_INTEGRACION.md
├── COBERTURA_TESTING.md
├── API_FRONTEND_APP.md
├── API_FRONTEND_APPWEB.md
└── API_FRONTEND_COMERCIAL.md
```

**Nota**: Documentación de desarrollo interno (instrucciones para equipos, guías de migración, análisis de consistencia, validaciones internas) se encuentra en el directorio `CHALLA/` (excluido del repositorio).

## Uso Rápido

- **Para desarrolladores frontend**: Comenzar con `APIS_INTEGRACION.md`
- **Para testing**: Ver `COBERTURA_TESTING.md`
- **Para referencia de API**: Usar `API_DOCUMENTACION.md` o Swagger UI

