# Configuración Railway

## Pasos para Desplegar

### 1. Conectar Repositorio

1. Ir a [Railway](https://railway.app)
2. Crear nuevo proyecto o seleccionar proyecto existente
3. Conectar repositorio GitHub: `HecAguilaV/SIGA_BACKEND`
4. Seleccionar rama: `main` (por defecto)
5. Railway detectará automáticamente el `Dockerfile` y lo usará para construir la imagen

### 2. Configurar Variables de Entorno

En Railway, agregar las siguientes variables de entorno:

```
DATABASE_URL=jdbc:postgresql://postgresql-hector.alwaysdata.net:5432/hector_siga_db
DB_USER=hector
DB_PASSWORD=kike4466
GEMINI_API_KEY=tu_gemini_api_key_aqui
JWT_SECRET=tu_secret_key_super_seguro_cambiar_en_produccion
PORT=8080
ALLOWED_ORIGINS=https://siga-appweb.vercel.app,https://siga-web.vercel.app,http://localhost:5173,http://localhost:3000
```

**IMPORTANTE**: 
- Cambiar `JWT_SECRET` por uno seguro en producción
- Rotar `GEMINI_API_KEY` si fue expuesta anteriormente
- `PORT` lo asigna Railway automáticamente, pero se puede configurar
- Actualizar `ALLOWED_ORIGINS` con las URLs reales de tus frontends en Vercel

### 3. Configurar Build

Railway usará automáticamente:
- **Builder**: DOCKERFILE (detectado automáticamente o configurado en `railway.json`)
- **Dockerfile**: Multi-stage build optimizado para Spring Boot
  - Stage 1: Build con Gradle 8.5 + JDK 21
  - Stage 2: Runtime con JRE Alpine (imagen liviana ~276MB)
- **Health Check**: `/health` (configurado en `railway.json`)
- **Timeout**: 300ms
- El Dockerfile construye el JAR ejecutable de Spring Boot y lo ejecuta con `java -jar`

**Nota**: El proyecto está migrado a **Spring Boot 3.2.0** con Kotlin. Ya no usa Ktor.

### 4. Verificar Despliegue

1. Railway construirá la imagen Docker automáticamente (2-5 minutos)
2. Verificar logs en Railway dashboard:
   - Buscar mensaje: `Started SigaBackendApplicationKt`
   - Verificar conexión a base de datos: `HikariPool-1 - Start completed`
   - Verificar inicialización de esquemas: `Esquemas de base de datos inicializados correctamente`
3. Probar endpoint de health: `https://tu-proyecto.railway.app/health`
4. Debe retornar: `{"status":"healthy","database":"connected","timestamp":"..."}`
5. Probar Swagger: `https://tu-proyecto.railway.app/swagger-ui/index.html`

### 5. Obtener URL de Producción

Railway asignará una URL automáticamente, por ejemplo:
- `https://siga-backend-production.up.railway.app`
- O una URL personalizada si configuraste un dominio

**Esta URL debe configurarse en**:
- Variables de entorno de los frontends en Vercel como `API_URL` o `VITE_API_URL`
- Documentación de API (`docs/API_FRONTEND_*.md`)
- Actualizar `ALLOWED_ORIGINS` en Railway con las URLs de los frontends en producción

**Endpoints disponibles**:
- Health: `/health`
- Swagger UI: `/swagger-ui.html`
- API Docs: `/api-docs`
- API Base: `/api/*`

## Troubleshooting

### Error: "No se puede conectar a la base de datos"
- Verificar que `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` estén correctos
- Verificar que Always Data permita conexiones desde Railway

### Error: "Port already in use"
- Railway asigna el puerto automáticamente
- Verificar que `PORT` esté configurado o dejar que Railway lo asigne

### Error: "Build failed"
- Revisar logs en Railway (sección "Deployments" → "View Logs")
- Verificar que `build.gradle.kts` esté correcto
- Verificar que todas las dependencias estén disponibles
- Verificar que el Dockerfile esté en la raíz del proyecto
- Spring Boot genera un JAR ejecutable (`SIGA_Backend-1.0-SNAPSHOT.jar`), verificar que se cree correctamente
- Si el build tarda mucho, puede ser normal (primera vez descarga todas las dependencias)

### Error: "Application failed to start"
- Verificar variables de entorno (especialmente `DATABASE_URL`, `DB_USER`, `DB_PASSWORD`)
- Verificar que la base de datos esté accesible desde Railway
- Revisar logs para ver el error específico de Spring Boot

### Error: "Health check failed"
- Verificar que el endpoint `/health` esté accesible
- Verificar que el puerto esté configurado correctamente (Railway asigna `PORT` automáticamente)
- Aumentar `healthcheckTimeout` en `railway.json` si es necesario

## Notas

- **Framework**: Spring Boot 3.2.0 con Kotlin (migrado desde Ktor)
- **Base de datos**: PostgreSQL externa (AlwaysData) - configurada vía `DATABASE_URL`
- **Docker**: Multi-stage build optimizado (imagen final ~276MB)
- **Plan gratuito**: Railway tiene límites, pero suficiente para desarrollo/testing
- **Logs**: Disponibles en el dashboard de Railway en tiempo real
- **Reinicio automático**: Configurado para reiniciar hasta 10 veces si falla
- **Futuro**: Considerar usar Railway PostgreSQL para mejor integración y menor latencia

## Estructura del Proyecto

- **Backend**: Spring Boot REST API
- **ORM**: Spring Data JPA (Hibernate)
- **Autenticación**: JWT con Spring Security
- **Documentación**: Swagger/OpenAPI (SpringDoc)
- **Tests**: Spring Boot Test (21 tests pasando)
- **Deployment**: Docker + Railway

