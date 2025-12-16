# Especificación Técnica – SIGA WebApp Operativa

**Versión:** 2.0.0  
**Fecha:** 15 de diciembre de 2025  
**Responsable:** Liderazgo Front Operativo

## 1. Stack y Arquitectura

- **Framework:** SvelteKit 5 (SSR habilitado).  
- **Lenguaje:** TypeScript estricto.  
- **Estado:** Stores de Svelte + Zustand para caché crítico.  
- **UI:** Tailwind CSS + componentes corporativos.  
- **Testing:** Vitest + Playwright.  
- **CI/CD:** GitHub Actions → Vercel.

## 2. Estructura del Proyecto

```
src/
├── lib/
│   ├── components/       (componentes UI)
│   ├── services/         (API client)
│   ├── stores/           (estado compartido)
│   └── utils/            (helpers)
├── routes/
│   ├── +layout.svelte
│   ├── inventario/
│   ├── ventas/
│   ├── analitica/
│   └── api/              (proxys server-side)
└── app.d.ts
```

## 3. Integración con Backend

- Cliente HTTP: `ky` configurado con interceptores de token y manejo uniforme de errores.
- Auth: usa `POST /api/auth/login` y renueva con `POST /api/auth/refresh` (silencioso, cada 12h).
- Consumo principal: endpoints documentados en `docs/ENDPOINTS_OPERATIVOS.md` (secciones WebApp).
- Control de permisos: el JWT entrega `rol` y `permisos`; la UI habilita funcionalidades según scope.

## 4. Módulos Funcionales

1. **Dashboard:** métricas agregadas, consumo de `/api/saas/ventas` y `/api/saas/productos`.
2. **Inventario:** CRUD completo de productos y stock; validaciones preventivas en frontend.
3. **Ventas (pendiente):** UI diseñada y conectores listos; a la espera de publicación de `/api/saas/ventas`.
4. **Asistente:** integración con `/api/saas/chat` usando SSE para respuestas en streaming.
5. **Administración:** gestión de usuarios operativos y locales.

## 5. Seguridad y Observabilidad

- CSP estricta y sanitización de entradas sensibles.
- Logs front en Datadog (navegadores modernos) con trazabilidad correlacionada a request backend.
- Feature flags administrados con `LaunchDarkly` para desplegar capacidades a subconjuntos de empresas.

## 6. Testing y QA

- **Unit tests:** Vitest (85% cobertura mínima).
- **E2E:** Playwright corriendo en CI (flujos inventario y ventas).
- **Contratos:** Postman collection sincronizada (`tests/api/webapp`) ejecutada previo a release.

## 7. Roadmap 2026

- Integración con WebSockets para dashboards en vivo (Q2).
- Editor de flujos de trabajo personalizados (Q3).
- Localización completa (ES/EN/PT) con i18n (Q4).

---

> Para cambios mayores consultar a backend antes de introducir nuevos endpoints o parámetros.
