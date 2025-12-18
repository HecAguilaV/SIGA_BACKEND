# Documentación SIGA Backend

Este directorio contiene la documentación técnica esencial del backend de SIGA.

## Documentos Principales

### Documentación de Referencia

1. **[API Endpoints](./API_ENDPOINTS.md)**
   - Documentación completa y actualizada de todos los endpoints del backend
   - Especificaciones detalladas de request/response
   - Códigos de estado HTTP y manejo de errores
   - Notas importantes sobre autenticación, permisos y separación por empresa

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

5. **[Esquemas Database](./ESQUEMAS_DATABASE.md)**
   - Estructura de la base de datos
   - Relaciones entre tablas
   - Esquemas y migraciones

## Documentación Interactiva

### Swagger UI
Documentación interactiva disponible en:
- **Producción**: `https://siga-backend-production.up.railway.app/swagger-ui/index.html`
- **Local**: `http://localhost:8080/swagger-ui/index.html`

### OpenAPI Spec
Especificación OpenAPI disponible en:
- **JSON**: `https://siga-backend-production.up.railway.app/api-docs`
- **YAML**: `/openapi.yaml`

## Uso Rápido

- **Para desarrolladores frontend**: Comenzar con `API_ENDPOINTS.md`
- **Para entender el sistema**: Leer `FUENTE_VERDAD_BACKEND.md`
- **Para sincronización**: Usar `SINCRONIZACION_APPMOVIL_WEBAPP.md`
- **Para referencia de API**: Usar `API_ENDPOINTS.md` o Swagger UI

## Estructura

```
docs/
├── README.md (este archivo)
├── API_ENDPOINTS.md
├── FUENTE_VERDAD_BACKEND.md
├── SINCRONIZACION_APPMOVIL_WEBAPP.md
├── PLAN_ACCION_SINCRONIZACION_PERFECTA.md
└── ESQUEMAS_DATABASE.md
```


---

## Autor

> **Héctor Aguila**  
>> Un Soñador con Poca RAM