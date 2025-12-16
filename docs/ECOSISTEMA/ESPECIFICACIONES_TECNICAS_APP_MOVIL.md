# Especificaciones Técnicas – SIGA App Móvil (Android)

**Versión:** 1.3.0  
**Fecha:** 15 de diciembre de 2025  
**Contacto técnico:** Liderazgo App Móvil (Android)

## 1. Arquitectura y Stack

- **Lenguaje:** Kotlin 1.9  
- **UI:** Jetpack Compose + Material 3  
- **Patrón:** MVVM con ViewModel + StateFlow  
- **Networking:** Ktor Client (JSON + Auth)  
- **DI:** Hilt  
- **Persistencia local:** Room (modo offline roadmap 2026)

## 2. Estructura de Paquetes

```
com.siga.app
├── data
│   ├── remote (Ktor services, DTOs)
│   ├── repository (implementaciones)
│   └── local (Room)
├── domain (modelos y casos de uso)
├── ui (pantallas Compose + ViewModels)
└── core (utilidades, manejo de errores, auth)
```

## 3. Integración con Backend

- Base URL: `https://siga-backend-production.up.railway.app`
- Autenticación: JWT (`Authorization: Bearer <token>`)
- Endpoints consumidos: ver `docs/ENDPOINTS_OPERATIVOS.md` (secciones WebApp/App Móvil).
- Refresco de token: `POST /api/auth/refresh` ejecutado vía WorkManager.
- Reintento automático: Exponential backoff con límite de 3 intentos por request.

## 4. Funcionalidades Cubiertas

1. **Inventario:** consulta, búsqueda y ajustes de stock por local.  
2. **Productos:** creación rápida, edición básica y consulta de detalles.  
3. **Ventas (pendiente):** UI y lógica listas; quedará habilitada al publicar `/api/saas/ventas`.  
4. **Asistente Operativo:** invocación de `/api/saas/chat` con reconocimiento de voz.  
5. **Alertas:** recordatorios push cuando un stock cae bajo `cantidadMinima`.

## 5. Dependencias Relevantes (build.gradle.kts)

```kotlin
implementation("io.ktor:ktor-client-android:2.3.8")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
implementation("androidx.compose.material3:material3:1.2.1")
implementation("androidx.room:room-ktx:2.6.0")
implementation("com.google.ai.client:generativeai:0.5.0")
```

## 6. Estrategia de QA

- Tests unitarios con JUnit + Turbine (flujos).  
- Tests UI con Compose Testing + Firebase Test Lab (dispositivos físicos).  
- Monitoreo Crashlytics (100% release).

## 7. Roadmap 2026

- Sincronización offline completa (Q2).
- Integración con escáner de código de barras vía ML Kit (Q2).
- Modo inventario masivo con lector Bluetooth (Q3).

---

> Este documento debe evolucionar junto con el repositorio de la app móvil. Cambios mayores requieren sincronización con backend y QA.
