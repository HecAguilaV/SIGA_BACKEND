# Documentación SIGA Backend

Este directorio contiene la documentación técnica esencial del backend de SIGA.

## 📚 Documentos Principales

### Documentación de Referencia

1. **[Endpoints Completos por Equipo](./ENDPOINTS_COMPLETOS_POR_EQUIPO.md)**
   - Referencia completa de todos los endpoints organizados por frontend
   - Especificaciones de request/response
   - Problemas conocidos y soluciones

2. **[Fuente de Verdad Backend](./FUENTE_VERDAD_BACKEND.md)**
   - Documento maestro que define el estado actual del backend
   - Separación por empresa (multi-tenancy)
   - Arquitectura y decisiones técnicas

3. **[Sincronización App Móvil ↔ WebApp](./SINCRONIZACION_APPMOVIL_WEBAPP.md)**
   - Guía de sincronización entre aplicaciones operativas
   - Endpoints compartidos
   - Principios de sincronización automática

4. **[Plan de Acción Sincronización](./PLAN_ACCION_SINCRONIZACION_PERFECTA.md)**
   - Plan detallado para lograr sincronización perfecta
   - Fases de implementación
   - Checklist y validaciones

### Documentación Técnica

5. **[API Documentación](./API_DOCUMENTACION.md)**
   - Documentación completa de todos los endpoints
   - Códigos de estado HTTP
   - Validaciones y reglas de negocio

6. **[APIs Integración](./APIS_INTEGRACION.md)**
   - Guía de integración para frontends
   - Ejemplos de código (JavaScript, TypeScript, Kotlin)
   - Clientes API reutilizables
   - Mejores prácticas de integración

7. **[Esquemas Database](./ESQUEMAS_DATABASE.md)**
   - Estructura de la base de datos
   - Relaciones entre tablas
   - Esquemas y migraciones

## 🔗 Documentación Interactiva

### Swagger UI
Documentación interactiva disponible en:
- **Producción**: `https://siga-backend-production.up.railway.app/swagger-ui/index.html`
- **Local**: `http://localhost:8080/swagger-ui/index.html`

### OpenAPI Spec
Especificación OpenAPI disponible en:
- **JSON**: `https://siga-backend-production.up.railway.app/api-docs`
- **YAML**: `/openapi.yaml`

## 📖 Uso Rápido

- **Para desarrolladores frontend**: Comenzar con `ENDPOINTS_COMPLETOS_POR_EQUIPO.md`
- **Para entender el sistema**: Leer `FUENTE_VERDAD_BACKEND.md`
- **Para integración**: Usar `APIS_INTEGRACION.md`
- **Para referencia de API**: Usar `API_DOCUMENTACION.md` o Swagger UI

## 🏗️ Estructura

```
docs/
├── README.md (este archivo)
├── ENDPOINTS_COMPLETOS_POR_EQUIPO.md
├── FUENTE_VERDAD_BACKEND.md
├── SINCRONIZACION_APPMOVIL_WEBAPP.md
├── PLAN_ACCION_SINCRONIZACION_PERFECTA.md
├── API_DOCUMENTACION.md
├── APIS_INTEGRACION.md
└── ESQUEMAS_DATABASE.md
```

---

**Nota**: Documentación de desarrollo interno y verificaciones temporales se encuentra en el directorio `CHALLA/` (excluido del repositorio público).
