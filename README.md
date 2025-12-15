# SIGA Backend

Backend API REST para el Sistema Inteligente de Gestión de Activos (SIGA) - Mini ERP para PYMES chilenas.

## 🚀 Enlaces Rápidos

- **API Backend:** `https://siga-backend-production.up.railway.app`
- **Swagger UI:** `https://siga-backend-production.up.railway.app/swagger-ui/index.html`
- **Web Comercial:** `https://siga-webcomercial.vercel.app`
- **WebApp Operativa:** `https://siga-webapp.vercel.app`
- **App Móvil:** Disponible en Google Play Store

## 📋 Descripción

SIGA Backend es una API REST desarrollada con Spring Boot y Kotlin que proporciona servicios para la gestión de inventario, ventas, suscripciones y asistentes de inteligencia artificial conversacionales.

## 🛠️ Tecnologías

- **Lenguaje**: Kotlin 1.9.22
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA (Hibernate)
- **Base de Datos**: PostgreSQL
- **Autenticación**: JWT con Spring Security
- **IA**: Google Gemini 2.5 Flash-Lite
- **Documentación**: Swagger/OpenAPI (SpringDoc)
- **Build**: Gradle 8.5
- **Deployment**: Docker + Railway

## 📚 Documentación

### Documentación Principal

- **[Endpoints Completos por Equipo](./docs/ENDPOINTS_COMPLETOS_POR_EQUIPO.md)** - Referencia completa de todos los endpoints por frontend
- **[Fuente de Verdad Backend](./docs/FUENTE_VERDAD_BACKEND.md)** - Documento maestro que define el estado actual del backend
- **[Sincronización App Móvil ↔ WebApp](./docs/SINCRONIZACION_APPMOVIL_WEBAPP.md)** - Guía de sincronización entre aplicaciones
- **[Plan de Acción Sincronización](./docs/PLAN_ACCION_SINCRONIZACION_PERFECTA.md)** - Plan detallado para sincronización perfecta

### Documentación Interactiva

- **Swagger UI**: `https://siga-backend-production.up.railway.app/swagger-ui/index.html`
- **OpenAPI Spec**: `https://siga-backend-production.up.railway.app/api-docs`

## ⚙️ Requisitos Previos

- **JDK 21** o superior
- **Gradle 8.5** (incluido en el proyecto via `gradlew`)
- **PostgreSQL** (local o remoto)
- **Git**

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/HecAguilaV/SIGA_BACKEND.git
cd SIGA_Backend
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` o configurar variables de entorno:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/siga_db
DB_USER=postgres
DB_PASSWORD=password
JWT_SECRET=tu_secret_key_super_seguro
GEMINI_API_KEY=tu_api_key_gemini
ALLOWED_ORIGINS=https://siga-webcomercial.vercel.app,https://siga-webapp.vercel.app
```

**Variables de entorno requeridas**:
- `DATABASE_URL`: URL de conexión a PostgreSQL
- `DB_USER`: Usuario de la base de datos
- `DB_PASSWORD`: Contraseña de la base de datos
- `JWT_SECRET`: Clave secreta para firmar tokens JWT
- `GEMINI_API_KEY`: API Key de Google Gemini
- `ALLOWED_ORIGINS`: Orígenes permitidos para CORS (separados por coma)

### 3. Configurar Base de Datos

Las tablas deben crearse manualmente ejecutando los scripts SQL que se encuentran en:
```
src/main/resources/db/migrations/
```

**Ver instrucciones detalladas**: `src/main/resources/db/migrations/README.md`

## ▶️ Ejecución

### Desarrollo Local

```bash
# Ejecutar aplicación
./gradlew bootRun

# O construir y ejecutar JAR
./gradlew build
java -jar build/libs/SIGA_Backend-1.0-SNAPSHOT.jar
```

El servidor estará disponible en `http://localhost:8080`

### Verificar que Funciona

```bash
# Health check
curl http://localhost:8080/health

# Debe retornar:
# {"status":"healthy","database":"connected","timestamp":"..."}
```

## 📡 Endpoints Principales

### Autenticación

- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/refresh` - Renovar token de acceso
- `GET /api/auth/me` - Obtener perfil actual

### Productos (Requiere autenticación + suscripción activa)

- `GET /api/saas/productos` - Listar productos
- `GET /api/saas/productos/{id}` - Obtener producto
- `POST /api/saas/productos` - Crear producto (solo ADMIN)
- `PUT /api/saas/productos/{id}` - Actualizar producto (solo ADMIN)
- `DELETE /api/saas/productos/{id}` - Eliminar producto (solo ADMIN)

### Stock (Requiere autenticación + suscripción activa)

- `GET /api/saas/stock` - Listar stock
- `POST /api/saas/stock` - Crear o actualizar stock

### Locales (Requiere autenticación + suscripción activa)

- `GET /api/saas/locales` - Listar locales
- `POST /api/saas/locales` - Crear local (solo ADMIN)
- `PUT /api/saas/locales/{id}` - Actualizar local (solo ADMIN)

### Ventas (Requiere autenticación + suscripción activa)

- `GET /api/saas/ventas` - Listar ventas
- `POST /api/saas/ventas` - Crear venta

### Planes (Público)

- `GET /api/comercial/planes` - Listar planes
- `GET /api/comercial/planes/{id}` - Obtener plan

### Suscripciones (Requiere autenticación)

- `GET /api/comercial/suscripciones` - Listar suscripciones del usuario
- `POST /api/comercial/suscripciones` - Crear suscripción

### Asistentes IA

- `POST /api/comercial/chat` - Asistente comercial (público)
- `POST /api/saas/chat` - Asistente operativo (requiere autenticación)

### Health Check

- `GET /health` - Estado del servidor y base de datos

## 🔐 Autenticación

### Obtener Token

```bash
# Login
curl -X POST https://siga-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123"
  }'
```

### Usar Token

```bash
# Incluir en header Authorization
curl -X GET https://siga-backend-production.up.railway.app/api/saas/productos \
  -H "Authorization: Bearer eyJhbGc..."
```

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Sin tests (más rápido para build)
./gradlew build -x test
```

## 🐳 Docker

### Construir Imagen

```bash
docker build -t siga-backend .
```

### Ejecutar Contenedor

```bash
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e DB_USER=user \
  -e DB_PASSWORD=pass \
  -e JWT_SECRET=secret \
  -e GEMINI_API_KEY=key \
  --name siga-backend \
  siga-backend
```

## 🚂 Despliegue en Railway

### Pasos Rápidos

1. **Conectar Repositorio**
   - Ir a [Railway](https://railway.app)
   - Crear nuevo proyecto
   - Conectar repositorio GitHub: `HecAguilaV/SIGA_BACKEND`
   - Seleccionar rama: `main`

2. **Configurar Variables de Entorno**
   - `DATABASE_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `GEMINI_API_KEY`
   - `JWT_SECRET`
   - `ALLOWED_ORIGINS`

3. **Deploy Automático**
   - Railway detectará el `Dockerfile` automáticamente
   - Construirá y desplegará la aplicación
   - URL disponible en el dashboard

Ver documentación completa en [RAILWAY.md](./RAILWAY.md)

## 📝 Comandos Útiles

```bash
# Compilar proyecto
./gradlew build

# Ejecutar aplicación
./gradlew bootRun

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean

# Construir sin tests (más rápido)
./gradlew build -x test
```

## 🏗️ Arquitectura

### Separación por Empresa

El backend implementa separación completa de datos por empresa. Cada usuario comercial tiene sus propios datos completamente aislados:

- ✅ Usuarios operativos separados por empresa
- ✅ Productos separados por empresa
- ✅ Locales separados por empresa
- ✅ Categorías separadas por empresa
- ✅ Stock separado por empresa
- ✅ Ventas separadas por empresa

El filtrado es automático en el backend basado en el token JWT del usuario autenticado.

### Multi-tenancy

- Cada empresa tiene su propio `usuario_comercial_id`
- Todos los datos operativos están asociados a una empresa
- Los endpoints filtran automáticamente por empresa del usuario

## 📄 Licencia

Copyright (c) 2025 Héctor Aguila - All Rights Reserved

Este software es propiedad privada. No se permite su uso comercial sin autorización.

---

## 👤 Autor

> **Héctor Aguila**  
> Un Soñasor con Poca RAM
