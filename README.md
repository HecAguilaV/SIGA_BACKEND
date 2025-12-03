# SIGA Backend

Backend API REST para el Sistema Inteligente de Gestión de Activos (SIGA).

## Tecnologías

- Kotlin 1.9.22
- Spring Boot 3.2.0
- Spring Data JPA (Hibernate)
- PostgreSQL (Always Data)
- JWT Authentication (Spring Security)
- Google Gemini 1.5 Flash (IA)
- Swagger/OpenAPI (SpringDoc)

## Estructura del Proyecto

```
src/main/kotlin/com/siga/backend/
├── controller/             # REST Controllers (Spring Boot)
│   ├── AuthController.kt
│   ├── ProductosController.kt
│   ├── StockController.kt
│   ├── VentasController.kt
│   ├── PlanesController.kt
│   ├── SuscripcionesController.kt
│   ├── ChatController.kt
│   └── HealthController.kt
├── service/                # Servicios de negocio
│   ├── JWTService.kt
│   ├── PasswordService.kt
│   ├── SubscriptionService.kt
│   ├── GeminiService.kt
│   ├── CommercialAssistantService.kt
│   └── OperationalAssistantService.kt
├── entity/                 # Entidades JPA
│   ├── UsuarioSaas.kt
│   ├── UsuarioComercial.kt
│   ├── Producto.kt
│   ├── Stock.kt
│   ├── Venta.kt
│   └── ...
├── repository/             # Repositorios JPA
├── config/                 # Configuración Spring Boot
│   ├── SecurityConfig.kt
│   ├── JwtAuthenticationFilter.kt
│   ├── DatabaseInitializer.kt
│   └── SwaggerConfig.kt
└── utils/                  # Utilidades
    └── SecurityUtils.kt
```

## Configuración

Las variables de entorno se configuran en `application.yml` o como variables de entorno del sistema:

**Variables requeridas**:
   - `DATABASE_URL`: URL de PostgreSQL
   - `DB_USER`: Usuario de BD
   - `DB_PASSWORD`: Contraseña de BD
   - `GEMINI_API_KEY`: API Key de Google Gemini
   - `JWT_SECRET`: Secret para JWT
   - `PORT`: Puerto del servidor (default: 8080)
   - `ALLOWED_ORIGINS`: Orígenes permitidos para CORS

## Base de Datos

### Inicialización

Los esquemas (`siga_saas` y `siga_comercial`) se crean automáticamente al iniciar la aplicación mediante `DatabaseInitializer`.

**Nota**: Las tablas deben crearse manualmente mediante scripts SQL o herramientas de migración.

Verificar tablas:
```bash
./gradlew verifyTables
```

### Esquemas

- `siga_saas`: Sistema operativo (productos, stock, ventas)
- `siga_comercial`: Portal comercial (planes, suscripciones)

## Ejecución

### Desarrollo Local

```bash
./gradlew bootRun
```

O usando Spring Boot directamente:
```bash
./gradlew build
java -jar build/libs/SIGA_Backend-1.0-SNAPSHOT.jar
```

El servidor estará disponible en `http://localhost:8080`

### Documentación API

Swagger UI disponible en:
```
http://localhost:8080/swagger-ui.html
```

API Docs (JSON):
```
http://localhost:8080/api-docs
```

## Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/refresh` - Renovar token

### Productos
- `GET /api/saas/productos` - Listar productos
- `GET /api/saas/productos/{id}` - Obtener producto
- `POST /api/saas/productos` - Crear producto (ADMIN)
- `PUT /api/saas/productos/{id}` - Actualizar producto (ADMIN)
- `DELETE /api/saas/productos/{id}` - Eliminar producto (ADMIN)

### Stock
- `GET /api/saas/stock` - Listar stock
- `POST /api/saas/stock` - Actualizar stock

### Ventas
- `GET /api/saas/ventas` - Listar ventas
- `POST /api/saas/ventas` - Crear venta

### Planes
- `GET /api/comercial/planes` - Listar planes (público)
- `GET /api/comercial/planes/{id}` - Obtener plan (público)

### Suscripciones
- `GET /api/comercial/suscripciones` - Listar suscripciones del usuario
- `POST /api/comercial/suscripciones` - Crear suscripción

### Asistentes IA
- `POST /api/comercial/chat` - Asistente comercial (público)
- `POST /api/saas/chat` - Asistente operativo (autenticado)

## Testing

Ejecutar todos los tests:
```bash
./gradlew test
```

Ejecutar test específico:
```bash
./gradlew test --tests "com.siga.backend.controller.AuthControllerTest"
./gradlew test --tests "com.siga.backend.service.JWTServiceTest"
```

**Tests disponibles**:
- 13 tests de servicios (JWT, Password)
- 8 tests de controllers (Auth, Productos)
- Total: 21 tests pasando

## Despliegue

### Railway

1. Conectar repositorio a Railway (rama `main`)
2. Railway detectará automáticamente el `Dockerfile`
3. Configurar variables de entorno en Railway
4. El servidor se construirá y desplegará automáticamente

**Build**: Docker multi-stage (Gradle build + JRE runtime)
**Health Check**: `/health` (timeout: 300ms)

**URL de Producción**: Verificar en Railway dashboard (ej: `https://siga-backend-production.up.railway.app`)

### Variables de Entorno en Railway

Configurar las mismas variables que en `.env`:
- `DATABASE_URL`
- `DB_USER`
- `DB_PASSWORD`
- `GEMINI_API_KEY`
- `JWT_SECRET`
- `PORT` (Railway lo asigna automáticamente)
- `ALLOWED_ORIGINS`

### Configuración en Frontends (Vercel)

Configurar en cada proyecto de Vercel la variable de entorno:
- `API_URL` = `https://siga-backend-production.up.railway.app`

## Licencia

Copyright (c) 2025 Héctor Aguila - All Rights Reserved

Este software es propiedad privada. No se permite su uso comercial sin autorización.

Propietario - SIGA


**Desarrollado por**
> **Héctor Aguila**
>> ###### Un Soñador con Poca RAM 🧑🏼‍💻