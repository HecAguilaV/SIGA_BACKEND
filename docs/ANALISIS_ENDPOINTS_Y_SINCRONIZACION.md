# Análisis de Endpoints y Sincronización App/Web

**Fecha de corte:** 15 de diciembre de 2025  
**Responsables:** Equipo Backend SIGA · Líderes WebApp, Web Comercial y App Móvil

Este documento describe cómo los clientes oficiales consumen los endpoints del backend, los flujos de sincronización y los acuerdos de diseño que aseguran consistencia de datos en el ecosistema SIGA.

## 1. Principios Clave

1. **Un backend, múltiples clientes:** todas las experiencias (portal comercial, operación web y app móvil) consumen la API desplegada en Railway.
2. **Separación por empresa:** cada request se ejecuta filtrado por `empresaId` derivado del JWT; no existe mezcla de información entre compañías.
3. **Sincronización en tiempo real:** cualquier alta/actualización queda disponible inmediatamente para todas las interfaces al reutilizar los mismos endpoints REST.
4. **Compatibilidad con IA:** los endpoints operativos alimentan interfaces humanas y agentes conversacionales.

## 2. Flujos Operativos Críticos

### 2.1 Gestión de Productos y Stock

1. WebApp crea o actualiza un producto (`POST/PUT /api/saas/productos`).
2. Se asegura stock inicial mediante `POST /api/saas/stock`.
3. El backend recalcula inventario y emite el JSON actualizado; App Móvil refresca listados con `GET /api/saas/stock`.
4. **Ventas operativas (en desarrollo):** los endpoints `/api/saas/ventas` se encuentran definidos en los esquemas, pero aún no están desplegados en producción; la sincronización se incorporará junto al módulo de ventas en Q1 2026.

### 2.2 Sincronización Portal Comercial → WebApp

1. El administrador comercial contrata o modifica un plan (`POST /api/comercial/suscripciones`).
2. El backend actualiza la relación empresa-plan y los permisos.
3. Los tokens operativos generados vía `/api/comercial/auth/obtener-token-operativo` incluyen el nuevo scope; WebApp refleja las capacidades en el próximo login.

### 2.3 IA Operativa

1. El usuario invoca `/api/saas/chat` con una intención (ej.: "incrementa stock del producto X").
2. El servicio IA normaliza la intención y llama internamente al endpoint (`POST /api/saas/stock`).
3. El resultado se registra en auditoría y queda disponible para WebApp/App Móvil al instante.

## 3. Matriz de Endpoints Compartidos

| Módulo | Endpoint | WebApp | App Móvil | Portal Comercial | Notas |
|--------|----------|--------|-----------|------------------|-------|
| Autenticación Operativa | `/api/auth/login` | ✔︎ | ✔︎ | ✖︎ | Base de acceso para operación. |
| Productos | `/api/saas/productos` | CRUD completo | Lectura + creación rápida | ✖︎ | App Móvil crea productos en modo inventario rápido. |
| Stock | `/api/saas/stock` | CRUD | Lectura + actualización puntual | ✖︎ | Payload homogéneo camelCase/snake_case. |
| Ventas | `/api/saas/ventas` | Alta y listado | Listado | ✖︎ | Descuento de stock automático. |
| Chat Comercial | `/api/comercial/chat` | ✖︎ | ✖︎ | ✔︎ | Captura leads y responde FAQ. |
| Chat Operativo | `/api/saas/chat` | ✔︎ | ✔︎ | ✖︎ | Resuelve operaciones vía IA. |

## 4. Estrategia de Consistencia

- **Transacciones ACID:** operaciones de ventas y stock se ejecutan dentro de transacciones PostgreSQL.
- **Idempotencia:** los endpoints de stock usan `productoId` + `localId`, facilitando reintentos sin duplicados.
- **Auditoría:** toda mutación genera evento en `siga_saas.eventos_auditoria` para trazabilidad.
- **Métricas:** Grafana monitorea latencias y códigos de respuesta; los incumplimientos SLA generan alertas en Slack `#siga-backend`.

## 5. Procedimiento ante Desalineaciones

1. El equipo cliente levanta issue en GitHub (labels `bug`, `docs`).
2. Backend revisa logs y especificaciones en `docs/API_ENDPOINTS.md`.
3. Se prioriza la corrección y se actualiza este documento con la resolución.

## 6. Roadmap de Mejora

- Pruebas de contrato (Pact) entre backend y WebApp – Q1 2026.
- WebSockets para notificaciones push de cambios críticos – Q2 2026.
- Homologación de payloads para integraciones con ERPs externos – 2026.

---

> Cualquier modificación estructural de los endpoints debe reflejarse aquí y en `docs/API_ENDPOINTS.md`.
