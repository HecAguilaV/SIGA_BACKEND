# Endpoints Operativos Verificados

Este documento consolida la verificación funcional de los endpoints expuestos por SIGA Backend y su consumo por los clientes oficiales (Web Comercial, WebApp Operativa y App Móvil Android). Es la referencia única para confirmar qué rutas están implementadas, probadas y en uso productivo.

## 1. Alcance y Metodología

- **Fuente de verdad técnica:** `docs/API_ENDPOINTS.md` (especificaciones detalladas).
- **Verificación cruzada:** revisión de código frontend (repositorios oficiales), pruebas manuales en Postman y revisión de métricas en producción.
- **Estado:** operativo, condicionado o no expuesto.
- **Última revisión:** 15 de diciembre de 2025.

## 2. Autenticación Comercial (`/api/comercial/auth`) – Consumo Web Comercial

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/register` | POST | Operativo | Web Comercial | Alta de cuentas con `nombreEmpresa` obligatorio. |
| `/login` | POST | Operativo | Web Comercial | Retorna `accessToken` + `refreshToken`. |
| `/refresh` | POST | Operativo | Web Comercial | Rotación de tokens. |
| `/reset-password` | POST | Operativo | Web Comercial | Envía token temporal vía correo. |
| `/change-password` | POST | Operativo | Web Comercial | Requiere token de `reset-password`. |
| `/update-email` | PUT | Operativo | Web Comercial | Valida contraseña antes de actualizar. |
| `/obtener-token-operativo` | POST | Operativo | Web Comercial, WebApp | Otorga token JWT para WebApp (SSO). |

## 3. Autenticación Operativa (`/api/auth`) – Consumo WebApp y App Móvil

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/login` | POST | Operativo | WebApp, App Móvil | JWT scopiado por empresa y rol. |
| `/register` | POST | Condicionado | Interno QA | Usado solo en entornos QA para bootstrap. |
| `/refresh` | POST | Operativo | WebApp | Renovación silenciosa. |
| `/me` | GET | Operativo | WebApp, App Móvil | Devuelve perfil y permisos efectivos. |

## 4. Gestión Operativa (`/api/saas`) – Consumo WebApp y App Móvil

### 4.1 Productos

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/productos` | GET | Operativo | WebApp, App Móvil | Soporta filtros por categoría y paginación. |
| `/productos/{id}` | GET | Operativo | WebApp | UI detalle producto. |
| `/productos` | POST | Operativo | WebApp | Crea con `precioUnitario`, `codigoBarras`, `categoriaId`. |
| `/productos/{id}` | PUT | Operativo | WebApp | Actualiza campos CRUD completos. |
| `/productos/{id}` | DELETE | Operativo | WebApp | Solo rol `ADMINISTRADOR`. |

### 4.2 Stock

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/stock` | GET | Operativo | WebApp, App Móvil | Filtro opcional por `localId`. |
| `/stock/{productoId}/{localId}` | GET | Operativo | WebApp | Validación inventario específico. |
| `/stock` | POST | Operativo | WebApp, App Móvil | Crea/actualiza stock; acepta camelCase y snake_case. |

### 4.3 Locales, Categorías y Ventas

| Dominio | Rutas | Estado | Clientes | Notas |
|---------|-------|--------|----------|-------|
| Locales | CRUD completo | Operativo | WebApp | Locales ligados a `usuarioComercialId`. |
| Categorías | CRUD completo | Operativo | WebApp | Altas y bajas preservan integridad. |
| Ventas | (pendiente de publicación) | No expuesto | N/A | En schemas y planificada para Q1 2026. |

## 5. Chat y Asistente IA

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/api/comercial/chat` | POST | Operativo | Web Comercial | Canal público para generación de leads. |
| `/api/saas/chat` | POST | Operativo | WebApp | Requiere suscripción activa y permisos. |

## 6. Portal Comercial (`/api/comercial/*`) – Gestión de Planes, Suscripciones y Facturas

| Dominio | Endpoints | Estado | Clientes |
|---------|-----------|--------|----------|
| Planes | `GET /planes`, `GET /planes/{id}` | Operativo | Web Comercial |
| Suscripciones | `GET /suscripciones`, `POST /suscripciones` | Operativo | Web Comercial |
| Facturas | `POST /facturas`, `GET /facturas`, `GET /facturas/{id}`, `GET /facturas/numero/{numero}` | Operativo | Web Comercial |

## 7. Consideraciones de Seguridad y Permisos

- **Scopes:** el JWT contiene `empresaId`, `usuarioId` y `rol`. Todo endpoint filtra por empresa.
- **Permisos granulares:** los endpoints de productos y stock están protegidos por `PRODUCTOS_*` y `STOCK_*`.
- **Suscripciones:** `/api/saas/chat` y componentes IA validan que la empresa tenga plan activo.

## 8. Matriz Cliente vs Endpoints

| Cliente | Autenticación | Gestión Operativa | Comercial | Chat |
|---------|---------------|-------------------|----------|------|
| Web Comercial | `/api/comercial/auth/*` | No | Sí | Sí (comercial) |
| WebApp | `/api/auth/*` | Sí | No | Sí (operativo) |
| App Móvil | `/api/auth/*` | Sí (consulta y ajustes de stock) | No | Sí (operativo, roadmap Q1 2026) |

## 9. Próximas Revisiones

- Automatizar verificación diaria vía tests contractuales (Newman + GitHub Actions).
- Exponer dashboard de monitoreo (Grafana) con métricas de éxito por endpoint.
- Extender documentación a endpoints internos de administración cuando pasen a producción.

---

> Cualquier discrepancia encontrada por los equipos frontend debe documentarse levantando un issue en GitHub (`labels: docs, backend`) y referenciar este documento.
