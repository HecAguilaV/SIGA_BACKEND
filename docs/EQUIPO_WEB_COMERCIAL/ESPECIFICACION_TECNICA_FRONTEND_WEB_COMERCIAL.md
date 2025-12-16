# Especificación Técnica – SIGA Portal Comercial

**Versión:** 1.5.0  
**Fecha:** 15 de diciembre de 2025  
**Responsable:** Liderazgo Web Comercial

## 1. Stack y Arquitectura

- **Framework:** React 18 + Vite.  
- **Lenguaje:** TypeScript con ESLint + Prettier.  
- **UI:** Bootstrap 5 + componentes propios.  
- **Estado:** React Query + Zustand (persistencia ligera).  
- **Routing:** React Router DOM.  
- **Testing:** Jest + Testing Library.  
- **Deployment:** Vercel (ambientes preview por PR).

## 2. Mapa de Módulos

- **Landing / Marketing:** páginas públicas con información comercial.
- **Planes y precios:** consumo de `/api/comercial/planes` para mostrar características.
- **Onboarding:** wizard de registro (usa `/api/comercial/auth/register`).
- **Administración de suscripciones:** panel privado que interactúa con `/api/comercial/suscripciones` y `/api/comercial/facturas`.
- **Chat Comercial:** integra `/api/comercial/chat` para captación y soporte.

## 3. Integración con Backend

- Base URL configurable vía `VITE_API_BASE_URL` (default Railway prod).
- JWT almacenado en `httpOnly cookies` para evitar XSS.
- Renovación de token automático usando `POST /api/comercial/auth/refresh`.
- Endpoint SSO (`/api/comercial/auth/obtener-token-operativo`) expuesto desde sección "Panel Operativo" para acceder a WebApp.

## 4. Flujo de Suscripción

1. Usuario inicia en landing y se registra (`/auth/register`).
2. tras verificar correo, ingresa y selecciona plan (`GET /planes`).
3. Al confirmar plan se crea la suscripción (`POST /suscripciones`).
4. Facturación se gestiona con `/api/comercial/facturas`; se muestra historial y PDFs.
5. Un job programado verifica renovaciones y alerta a backend ante fallas de pago.

## 5. Seguridad

- Protección CSRF con tokens synchronizer.
- Validaciones de entrada en backend y frontend.
- Rate limiting en endpoints públicos (`/comercial/auth/*`).
- Monitoreo Sentry + Logs en Datadog (`service: siga-webcomercial`).

## 6. Dependencias clave (package.json)

```json
{
	"dependencies": {
		"@tanstack/react-query": "5.35.0",
		"axios": "1.6.8",
		"bootstrap": "5.3.3",
		"react": "18.3.1",
		"react-router-dom": "6.26.2",
		"recharts": "3.3.0"
	}
}
```

## 7. Roadmap 2026

- Panel de métricas de uso de WebApp asociado a la cuenta (Q2).
- Integración con sistemas de pago alternativos regionales (Q3).
- Experiencia B2B para partners (Q4).

---

> Cualquier cambio que requiera nuevos endpoints comerciales debe coordinarse con backend y actualizar `docs/API_ENDPOINTS.md`.
