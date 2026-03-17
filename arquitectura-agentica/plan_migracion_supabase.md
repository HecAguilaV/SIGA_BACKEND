# Plan de Migración: Hacia Supabase & Nueva Infraestructura

Héctor, he analizado tu estructura actual y el `dump.sql`. Tienes una base de datos PostgreSQL 16.13 muy bien estructurada con el esquema `siga_comercial`. 

Aquí tienes la hoja de ruta para que SIGA recupere la vida y escale con cimientos de concreto:

## 1. El Desafío del Backend (Kotlin)
**IMPORTANTE:** Supabase es una plataforma de Base de Datos y Servicios (Baas), pero no permite ejecutar archivos `.jar` (Kotlin/Spring Boot) directamente. Supabase ofrece *Edge Functions*, pero son en TypeScript/Go.

### Estrategia de Hosting para SIGA-BACKEND
Para mantener tu código Kotlin intacto y sin "romper" nada, te sugiero estas opciones:
*   **Opción A: Render.com (Recomendado):** Muy similar a Railway, fácil de configurar desde GitHub y tiene un nivel gratuito/barato persistente.
*   **Opción B: Fly.io:** Excelente para baja latencia, pero un poco más complejo de configurar.
*   **Opción C: Railway (Plan "Hobby"):** Si ya te gustaba Railway, el plan de $5 USD/mes es muy estable y te quitas el dolor de cabeza de configurar todo de nuevo.

## 2. Migración de Base de Datos (AlwaysData -> Supabase)
Tus datos actuales en AlwaysData se pueden migrar a Supabase en minutos:
1.  **Exportación:** Ya tienes el `dump.sql` listo en `SIGA-BACKEND/scripts/dump.sql`.
2.  **Importación:** Solo debemos ejecutar este script en el editor de SQL de Supabase.
3.  **Esquemas:** Supabase usa el esquema `public` por defecto, pero tu dump crea `siga_comercial`. Esto es PERFECTO para mantener tu arquitectura Multi-tenant aislada.

## 3. Reducción de Latencia (Vercel + Supabase)
Al tener las WebApps en Vercel y la DB en Supabase, la latencia será mínima (ambos suelen usar infraestructura de AWS en las mismas regiones).

## 4. El Nuevo Paradigma: Agentes en Supabase
Aquí es donde se pone interesante para tu visión de Arquitecto:
*   Podemos usar **Supabase Edge Functions** exclusivamente para el **Asistente de IA (Agente)**. 
*   **Ventaja:** Si el backend Kotlin está muy cargado, el agente sigue funcionando independientemente. 
*   **Conexión Directa:** Las Edge Functions están "al lado" de la base de datos, lo que hace que las consultas del agente sean instantáneas.

## Pasos Inmediatos Sugeridos:
1.  **Crear Proyecto en Supabase:** Obtener la URL de conexión de Postgres.
2.  **Actualizar `application.yml`:** Cambiar las variables `DATABASE_URL`, `DB_USER` y `DB_PASSWORD` para apuntar a Supabase.
3.  **Desplegar Backend:** En Render o Railway (pagado) vinculando tu repo de GitHub. Vercel detectará el cambio y actualizará las WebApps automáticamente.

¿Qué te parece este plan? Si estás de acuerdo, podemos empezar preparando el `application.yml` para la nueva conexión.
