# SIGA Backend

Backend API REST para el Sistema Inteligente de Gestión de Activos (SIGA) - Mini ERP para PYMES chilenas.

## 📋 Descripción

SIGA Backend es una API REST desarrollada con Spring Boot y Kotlin que proporciona servicios para la gestión de inventario, ventas, suscripciones y asistentes de inteligencia artificial conversacionales.

## 🚀 Tecnologías

- **Lenguaje**: Kotlin 1.9.22
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA (Hibernate)
- **Base de Datos**: PostgreSQL
- **Autenticación**: JWT con Spring Security
- **IA**: Google Gemini 1.5 Flash
- **Documentación**: Swagger/OpenAPI (SpringDoc)
- **Build**: Gradle 8.5
- **Deployment**: Docker + Railway

## 📦 Requisitos Previos

- **JDK 21** o superior
- **Gradle 8.5** (incluido en el proyecto via `gradlew`)
- **PostgreSQL** (local o remoto)
- **Git**

## 🔧 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/HecAguilaV/SIGA_BACKEND.git
cd SIGA_Backend
```

### 2. Configurar Variables de Entorno

Crear archivo `src/main/resources/application.yml` o configurar variables de entorno del sistema:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/siga_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:password}
    
jwt:
  secret: ${JWT_SECRET:tu_secret_key_super_seguro}
  
gemini:
  api-key: ${GEMINI_API_KEY:tu_api_key_gemini}

cors:
  allowed-origins: ${ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}
```

**Variables de entorno requeridas**:
- `DATABASE_URL`: URL de conexión a PostgreSQL
- `DB_USER`: Usuario de la base de datos
- `DB_PASSWORD`: Contraseña de la base de datos
- `JWT_SECRET`: Clave secreta para firmar tokens JWT
- `GEMINI_API_KEY`: API Key de Google Gemini
- `ALLOWED_ORIGINS`: Orígenes permitidos para CORS (separados por coma)

### 3. Configurar Base de Datos

#### Opción A: PostgreSQL Local

1. Instalar PostgreSQL
2. Crear base de datos:
```sql
CREATE DATABASE siga_db;
```

3. Los esquemas (`siga_saas` y `siga_comercial`) se crean automáticamente al iniciar la aplicación.

#### Opción B: PostgreSQL Remoto

Configurar `DATABASE_URL` con la URL de tu servidor PostgreSQL remoto.

### 4. Crear Tablas

Las tablas deben crearse manualmente mediante scripts SQL. Ver estructura en `docs/` o usar herramientas de migración como Flyway o Liquibase.

**Esquemas**:
- `siga_saas`: Sistema operativo (productos, stock, ventas, usuarios)
- `siga_comercial`: Portal comercial (planes, suscripciones)

## 🏃 Ejecución

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

## 📚 Documentación API

### Swagger UI

Acceder a la interfaz interactiva de Swagger:
```
http://localhost:8080/swagger-ui.html
```

### API Docs (JSON)

Documentación OpenAPI en formato JSON:
```
http://localhost:8080/api-docs
```

## 🗂️ Estructura del Proyecto

```
src/main/kotlin/com/siga/backend/
├── controller/             # REST Controllers
│   ├── AuthController.kt          # Autenticación (register, login, refresh)
│   ├── ProductosController.kt     # CRUD Productos
│   ├── StockController.kt         # Gestión de Stock
│   ├── VentasController.kt        # Gestión de Ventas
│   ├── PlanesController.kt        # Planes de suscripción
│   ├── SuscripcionesController.kt # Suscripciones
│   ├── ChatController.kt          # Asistentes IA
│   └── HealthController.kt       # Health check
├── service/                # Servicios de negocio
│   ├── JWTService.kt             # Generación y validación de JWT
│   ├── PasswordService.kt        # Hashing de contraseñas (BCrypt)
│   ├── SubscriptionService.kt    # Validación de suscripciones
│   ├── GeminiService.kt          # Cliente para Gemini API
│   ├── CommercialAssistantService.kt  # Asistente comercial
│   └── OperationalAssistantService.kt  # Asistente operativo
├── entity/                 # Entidades JPA
│   ├── UsuarioSaas.kt
│   ├── UsuarioComercial.kt
│   ├── Producto.kt
│   ├── Stock.kt
│   ├── Venta.kt
│   ├── Plan.kt
│   ├── Suscripcion.kt
│   ├── Categoria.kt
│   └── Local.kt
├── repository/             # Repositorios JPA
│   ├── UsuarioSaasRepository.kt
│   ├── ProductoRepository.kt
│   └── ...
├── config/                 # Configuración Spring Boot
│   ├── SecurityConfig.kt          # Configuración de seguridad
│   ├── JwtAuthenticationFilter.kt # Filtro JWT
│   ├── DatabaseInitializer.kt     # Inicialización de esquemas
│   └── SwaggerConfig.kt          # Configuración Swagger
└── utils/                  # Utilidades
    └── SecurityUtils.kt          # Helpers para SecurityContext
```

## 🔌 Endpoints Principales

### Autenticación

- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/refresh` - Renovar token de acceso

### Productos (Requiere autenticación + suscripción activa)

- `GET /api/saas/productos` - Listar productos
- `GET /api/saas/productos/{id}` - Obtener producto
- `POST /api/saas/productos` - Crear producto (solo ADMIN)
- `PUT /api/saas/productos/{id}` - Actualizar producto (solo ADMIN)
- `DELETE /api/saas/productos/{id}` - Eliminar producto (solo ADMIN)

### Stock (Requiere autenticación + suscripción activa)

- `GET /api/saas/stock` - Listar stock
- `POST /api/saas/stock` - Actualizar stock

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
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123"
  }'

# Respuesta:
# {
#   "success": true,
#   "accessToken": "eyJhbGc...",
#   "refreshToken": "eyJhbGc...",
#   "user": { ... }
# }
```

### Usar Token

```bash
# Incluir en header Authorization
curl -X GET http://localhost:8080/api/saas/productos \
  -H "Authorization: Bearer eyJhbGc..."
```

## 🧪 Testing

### Ejecutar Todos los Tests

```bash
./gradlew test
```

### Ejecutar Test Específico

```bash
# Tests de servicios
./gradlew test --tests "com.siga.backend.service.JWTServiceTest"
./gradlew test --tests "com.siga.backend.service.PasswordServiceTest"

# Tests de controllers
./gradlew test --tests "com.siga.backend.controller.AuthControllerTest"
./gradlew test --tests "com.siga.backend.controller.ProductosControllerTest"
```

### Cobertura de Tests

- **21 tests pasando** (81% de cobertura)
- 13 tests de servicios (JWT, Password)
- 8 tests de controllers (Auth, Productos)

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

## 📖 Documentación Adicional

- [RAILWAY.md](./RAILWAY.md) - Guía de despliegue en Railway
- [docs/API_FRONTEND_APPWEB.md](./docs/API_FRONTEND_APPWEB.md) - Documentación API para frontend web
- [docs/API_FRONTEND_APP.md](./docs/API_FRONTEND_APP.md) - Documentación API para app móvil
- [docs/API_FRONTEND_COMERCIAL.md](./docs/API_FRONTEND_COMERCIAL.md) - Documentación API para portal comercial

## 🛠️ Comandos Útiles

```bash
# Compilar proyecto
./gradlew build

# Ejecutar aplicación
./gradlew bootRun

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean

# Ver dependencias
./gradlew dependencies

# Construir sin tests (más rápido)
./gradlew build -x test
```

## 🐛 Troubleshooting

### Error: "No se puede conectar a la base de datos"
- Verificar que PostgreSQL esté corriendo
- Verificar `DATABASE_URL`, `DB_USER`, `DB_PASSWORD`
- Verificar que la base de datos exista

### Error: "Port already in use"
- Cambiar puerto en `application.yml`: `server.port: 8081`
- O matar proceso en puerto 8080

### Error: "JWT_SECRET not set"
- Configurar variable de entorno `JWT_SECRET`
- O agregar en `application.yml`

### Error en Tests
- Verificar que la base de datos de test esté configurada
- Ejecutar `./gradlew clean test`

## 📝 Licencia

Copyright (c) 2025 Héctor Aguila - All Rights Reserved

Este software es propiedad privada. No se permite su uso comercial sin autorización.

## 👤 Autor

**Héctor Aguila**

---

**Desarrollado con ❤️ para PYMES chilenas**
