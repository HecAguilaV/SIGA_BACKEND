# Estado Actual del Ecosistema SIGA

Este documento resume el estado de los componentes del Proyecto SIGA al **17 de marzo de 2026**.

## 📊 Resumen de Componentes

| Servicio | Estado Git | Tecnología | Propósito |
| :--- | :--- | :--- | :--- |
| **SIGA-BACKEND** | ✅ Sincronizado | Kotlin / Spring Boot | API Central, Gestión de IA y DB. |
| **SIGA-WEBCOMERCIAL** | ✅ Sincronizado | React + Vite | Landing page, registro y mantenimiento. |
| **SIGA-WEBAPP** | ✅ Sincronizado | SvelteKit | Panel administrativo para dueños. |
| **SIGA-APP** | ✅ Sincronizado | Android (Compose) | Operación en terreno y stock. |

## 🛠️ Detalles Técnicos y Logros Recientes

### SIGA-BACKEND (Repositorio Remoto Actualizado)
- **Migraciones**: Se ha añadido y pusheado el script `015_ai_fallback_functions.sql` que garantiza la continuidad del negocio mediante **SafeMode** (funciones SQL) si la IA falla.
- **Preparación Supabase**: El archivo `application.yml` está pre-configurado para facilitar la transición desde AlwaysData.
- **Seguridad**: Autenticación JWT implementada y funcional.

### SIGA-WEBCOMERCIAL (Repositorio Remoto Actualizado)
- **Modo Mantenimiento**: Implementada la `MigrationPage.jsx`. El sistema ahora puede redirigir usuarios automáticamente durante migraciones críticas.
- **Integración API**: Configurada para detectar caídas del backend y activar el fallback visual.

### Arquitectura Base
- **Multi-tenancy**: Estructura sólida basada en `usuario_comercial_id`.
- **Base de Datos**: Esquemas separados (`siga_comercial` y `siga_saas`) en PostgreSQL 16.

## 🧭 Recomendación: Arquitectura Agéntica

Respecto a la **Arquitectura Agéntica**, mi recomendación es integrarla de la siguiente manera:

1.  **Directorio Central**: Mantener la carpeta raíz `SIGA/arquitectura-agentica` como el **Cerebro del Proyecto**. Es el lugar ideal para los planes de migración, documentos de diseño y guías de agentes (SDD).
2.  **Documentación de Código**:
    -   **Backend**: Añadir un archivo `AGENTIC.md` dentro de `SIGA-BACKEND` que explique cómo el código Kotlin interactúa con los prompts y las funciones de fallback.
    -   **WebApps**: Documentar el flujo de "Conversación a Acción" en los servicios de chat.
3.  **Memoria del Sistema**: Seguir utilizando el sistema de **Engram** para que yo (u otros agentes) recordemos decisiones arquitectónicas cruzadas entre repositorios.

---
*Documento generado automáticamente para seguimiento de estado.*
