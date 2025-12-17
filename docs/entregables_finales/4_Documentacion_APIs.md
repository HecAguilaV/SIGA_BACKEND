# Documentación de APIs - SIGA Backend

## Información General
- **Base URL**: `https://siga-backend-production.up.railway.app`
- **Versión**: v1.0.0
- **Formato**: JSON
- **Autenticación**: Bearer Token (JWT)

## Swagger / OpenAPI
La documentación interactiva completa y ejecutable está disponible en:
👉 **[Swagger UI - SIGA Backend](https://siga-backend-production.up.railway.app/swagger-ui)**

---

## Endpoints Principales

### 🔐 Autenticación (`/api/auth`)
- `POST /login`: Inicia sesión y devuelve Access/Refresh tokens.
- `POST /register`: (Uso interno/WebComercial) Registra nuevos tenant admins.

### 📦 Inventario SaaS (`/api/saas`)
- `GET /productos`: Listado paginado de productos del tenant.
- `POST /productos`: Crear nuevo producto.
- `GET /stock`: Consultar stock consolidado o por local.
- `PUT /stock/{id}`: Ajuste manual de inventario.

### 🤖 Asistente IA (`/api/chat`)
- `POST /comercial`: Chatbot para Web Comercial (información de ventas/planes).
- `POST /operativo`: (Planificado) Chatbot para WebApp (acciones operativas).

### 🏪 Gestión (`/api/admin`)
- `GET /locales`: Gestión de sucursales físicas.
- `GET /usuarios`: Gestión de equipo de trabajo y roles.

## Códigos de Estado
- `200 OK`: Operación exitosa.
- `201 Created`: Recurso creado.
- `400 Bad Request`: Error de validación.
- `401 Unauthorized`: Token inválido o expirado.
- `403 Forbidden`: Falta de permisos (Rol insuficiente).
- `500 Server Error`: Error interno no controlado.
