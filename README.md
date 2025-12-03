# SIGA Backend

Backend API REST para el Sistema Inteligente de Gestión de Activos (SIGA).

## Tecnologías

- Kotlin 1.9.22
- Ktor 2.3.5
- PostgreSQL (Always Data)
- Exposed ORM
- JWT Authentication
- Google Gemini 2.5 Flash (IA)

## Estructura del Proyecto

```
src/main/kotlin/com/siga/backend/
├── api/                    # Endpoints REST
│   ├── auth/              # Autenticación
│   ├── chat/              # Asistentes IA
│   ├── productos/         # CRUD Productos
│   ├── stock/             # Gestión de Stock
│   ├── ventas/            # Gestión de Ventas
│   ├── planes/             # Planes de suscripción
│   └── suscripciones/      # Suscripciones
├── config/                 # Configuración
├── models/                 # Modelos Exposed
├── services/               # Servicios de negocio
└── utils/                  # Utilidades
```

## Configuración

1. Copiar `.env.example` a `.env`
2. Configurar variables de entorno:
   - `DATABASE_URL`: URL de PostgreSQL
   - `DB_USER`: Usuario de BD
   - `DB_PASSWORD`: Contraseña de BD
   - `GEMINI_API_KEY`: API Key de Google Gemini
   - `JWT_SECRET`: Secret para JWT
   - `PORT`: Puerto del servidor (default: 8080)
   - `ALLOWED_ORIGINS`: Orígenes permitidos para CORS

## Base de Datos

### Migraciones

Ejecutar migraciones:
```bash
./gradlew migrate
```

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
./gradlew run
```

El servidor estará disponible en `http://localhost:8080`

### Documentación API

Swagger UI disponible en:
```
http://localhost:8080/swagger-ui
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

Ejecutar tests:
```bash
./gradlew test
```

Ejecutar test específico:
```bash
./gradlew test --tests "com.siga.backend.AuthTest"
```

## Despliegue

### Railway

1. Conectar repositorio a Railway
2. Configurar variables de entorno en Railway
3. Railway detectará automáticamente el proyecto Kotlin
4. El servidor se desplegará automáticamente

**URL de Producción**: `https://siga-backend-production.up.railway.app`

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