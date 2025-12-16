# Endpoints Operativos Verificados (Diciembre 2025)

Este documento consolida la verificación funcional de los endpoints expuestos por SIGA Backend y su consumo por los clientes oficiales (Web Comercial, WebApp Operativa y App Móvil Android). Es la referencia única para confirmar qué rutas están implementadas, probadas y en uso productivo.

## 1. Alcance y Metodología

- **Fuente de verdad técnica:** `docs/API_ENDPOINTS.md` (especificaciones detalladas).
- **Verificación cruzada:** revisión de código frontend (repositorios oficiales), pruebas manuales en Postman y revisión de métricas en producción.
- **Estado:** ✔︎ operativo, ⚠︎ condicionado, ✖︎ no expuesto.
- **Última revisión:** 15 de diciembre de 2025.

## 2. Autenticación Comercial (`/api/comercial/auth`) – Consumo Web Comercial

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/register` | POST | ✔︎ | Web Comercial | Alta de cuentas con `nombreEmpresa` obligatorio. |
| `/login` | POST | ✔︎ | Web Comercial | Retorna `accessToken` + `refreshToken`. |
| `/refresh` | POST | ✔︎ | Web Comercial | Rotación de tokens. |
| `/reset-password` | POST | ✔︎ | Web Comercial | Envía token temporal vía correo. |
| `/change-password` | POST | ✔︎ | Web Comercial | Requiere token de `reset-password`. |
| `/update-email` | PUT | ✔︎ | Web Comercial | Valida contraseña antes de actualizar. |
| `/obtener-token-operativo` | POST | ✔︎ | Web Comercial, WebApp | Otorga token JWT para WebApp (SSO). |

## 3. Autenticación Operativa (`/api/auth`) – Consumo WebApp y App Móvil

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/login` | POST | ✔︎ | WebApp, App Móvil | JWT scopiado por empresa y rol. |
| `/register` | POST | ⚠︎ | Interno QA | Usado solo en entornos QA para bootstrap. |
| `/refresh` | POST | ✔︎ | WebApp | Renovación silenciosa. |
| `/me` | GET | ✔︎ | WebApp, App Móvil | Devuelve perfil y permisos efectivos. |

## 4. Gestión Operativa (`/api/saas`) – Consumo WebApp y App Móvil

### 4.1 Productos

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/productos` | GET | ✔︎ | WebApp, App Móvil | Soporta filtros por categoría y paginación. |
| `/productos/{id}` | GET | ✔︎ | WebApp | UI detalle producto. |
| `/productos` | POST | ✔︎ | WebApp | Crea con `precioUnitario`, `codigoBarras`, `categoriaId`. |
| `/productos/{id}` | PUT | ✔︎ | WebApp | Actualiza campos CRUD completos. |
| `/productos/{id}` | DELETE | ✔︎ | WebApp | Solo rol `ADMINISTRADOR`. |

### 4.2 Stock

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/stock` | GET | ✔︎ | WebApp, App Móvil | Filtro opcional por `localId`. |
| `/stock/{productoId}/{localId}` | GET | ✔︎ | WebApp | Validación inventario específico. |
| `/stock` | POST | ✔︎ | WebApp, App Móvil | Crea/actualiza stock; acepta camelCase y snake_case. |

### 4.3 Locales, Categorías y Ventas

| Dominio | Rutas | Estado | Clientes | Notas |
|---------|-------|--------|----------|-------|
| Locales | CRUD completo | ✔︎ | WebApp | Locales ligados a `usuarioComercialId`. |
| Categorías | CRUD completo | ✔︎ | WebApp | Altas y bajas preservan integridad. |
| Ventas | (pendiente de publicación) | ✖︎ | — | En schemas y planificada para Q1 2026. |

## 5. Chat y Asistente IA

| Endpoint | Método | Estado | Clientes | Notas |
|----------|--------|--------|----------|-------|
| `/api/comercial/chat` | POST | ✔︎ | Web Comercial | Canal público para generación de leads. |
| `/api/saas/chat` | POST | ✔︎ | WebApp | Requiere suscripción activa y permisos. |

## 6. Portal Comercial (`/api/comercial/*`) – Gestión de Planes, Suscripciones y Facturas

| Dominio | Endpoints | Estado | Clientes |
|---------|-----------|--------|----------|
| Planes | `GET /planes`, `GET /planes/{id}` | ✔︎ | Web Comercial |
| Suscripciones | `GET /suscripciones`, `POST /suscripciones` | ✔︎ | Web Comercial |
| Facturas | `POST /facturas`, `GET /facturas`, `GET /facturas/{id}`, `GET /facturas/numero/{numero}` | ✔︎ | Web Comercial |

## 7. Consideraciones de Seguridad y Permisos

- **Scopes:** el JWT contiene `empresaId`, `usuarioId` y `rol`. Todo endpoint filtra por empresa.
- **Permisos granulares:** los endpoints de productos y stock están protegidos por `PRODUCTOS_*` y `STOCK_*`.
- **Suscripciones:** `/api/saas/chat` y componentes IA validan que la empresa tenga plan activo.

## 8. Matriz Cliente vs Endpoints

| Cliente | Autenticación | Gestión Operativa | Comercial | Chat |
|---------|---------------|-------------------|----------|------|
| Web Comercial | `/api/comercial/auth/*` | ✖︎ | ✔︎ | ✔︎ (comercial) |
| WebApp | `/api/auth/*` | ✔︎ | ✖︎ | ✔︎ (operativo) |
| App Móvil | `/api/auth/*` | ✔︎ (consulta y ajustes stock) | ✖︎ | ✔︎ (operativo, roadmap Q1 2026) |

## 9. Próximas Revisiones

- Automatizar verificación diaria vía tests contractuales (Newman + GitHub Actions).
- Exponer dashboard de monitoreo (Grafana) con métricas de éxito por endpoint.
- Extender documentación a endpoints internos de administración cuando pasen a producción.

---

> Cualquier discrepancia encontrada por los equipos frontend debe documentarse levantando un issue en GitHub (`labels: docs, backend`) y referenciar este documento.
