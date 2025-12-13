# 📤 Guía: Qué Documentos Compartir con Cada Equipo

## 🎯 Resumen Rápido

**Para TODOS los equipos frontend:**
- ✅ `docs/ESTADO_BACKEND_COMPLETO.md` - Estado general del backend

**Solo para Web Comercial:**
- ✅ `CHALLA/docs/INSTRUCCIONES_FRONTENDS_BREVE.md` (secciones 1, 2, 2.1, 3)
- ✅ `CHALLA/docs/webcomercial/INSTRUCCIONES_ASISTENTE_IA.md`
- ✅ `CHALLA/docs/webcomercial/INSTRUCCIONES_UPDATE_EMAIL.md`

**Solo para WebApp:**
- ✅ `CHALLA/docs/appweb/SOLUCION_PERMISOS_ADMINISTRADOR.md`

**Solo para App Móvil:**
- ✅ `CHALLA/docs/CREDENCIALES_PRUEBA_APPMOVIL.md`

**NO compartir con frontends (solo backend/DBA):**
- ❌ `docs/MIGRACIONES_COMPLETAS.md` - Solo para quien maneja la BD
- ❌ `src/main/resources/db/migrations/VERIFICACION_TABLAS.sql` - Solo para DBA

---

## 📋 Documentos por Equipo

### 1. Web Comercial (Portal de Ventas)

**Documentos a compartir:**

1. **`docs/ESTADO_BACKEND_COMPLETO.md`**
   - **Por qué:** Estado general del backend, endpoints, planes, trial
   - **Qué les dice:** Cómo funciona el backend en general

2. **`CHALLA/docs/INSTRUCCIONES_FRONTENDS_BREVE.md`**
   - **Por qué:** Instrucciones específicas de cambios recientes
   - **Secciones relevantes:**
     - Sección 1: Campo `nombreEmpresa` en registro
     - Sección 2: Reset de contraseña
     - Sección 2.1: Actualizar email
     - Sección 3: Asistente IA (corrección importante)

3. **`CHALLA/docs/webcomercial/INSTRUCCIONES_ASISTENTE_IA.md`**
   - **Por qué:** Corrección crítica - NO usar API key directamente
   - **Qué les dice:** Cómo usar el endpoint del backend para el asistente

4. **`CHALLA/docs/webcomercial/INSTRUCCIONES_UPDATE_EMAIL.md`**
   - **Por qué:** Instrucciones detalladas para implementar cambio de email
   - **Qué les dice:** Cómo implementar el formulario de cambio de email

**Resumen para Web Comercial:**
- Campo `nombreEmpresa` en registro
- Reset de contraseña (token se retorna en respuesta)
- Actualizar email (endpoint PUT)
- Asistente IA (usar endpoint del backend, no API key directa)

---

### 2. WebApp (Aplicación Operativa)

**Documentos a compartir:**

1. **`docs/ESTADO_BACKEND_COMPLETO.md`**
   - **Por qué:** Estado general del backend, endpoints, sistema de permisos
   - **Qué les dice:** Cómo funciona el backend, qué endpoints usar

2. **`CHALLA/docs/appweb/SOLUCION_PERMISOS_ADMINISTRADOR.md`**
   - **Por qué:** Problema resuelto de permisos para ADMINISTRADOR
   - **Qué les dice:** Ya no necesitan el workaround, el backend maneja permisos correctamente

**Resumen para WebApp:**
- Sistema de permisos funcionando (ADMINISTRADOR tiene todos automáticamente)
- Endpoints operativos documentados
- Ya no necesitan manejar el caso especial de ADMINISTRADOR sin permisos

---

### 3. App Móvil

**Documentos a compartir:**

1. **`docs/ESTADO_BACKEND_COMPLETO.md`**
   - **Por qué:** Estado general del backend, endpoints de autenticación
   - **Qué les dice:** Cómo funciona el backend, qué endpoints usar

2. **`CHALLA/docs/CREDENCIALES_PRUEBA_APPMOVIL.md`**
   - **Por qué:** Credenciales de prueba y cómo registrar usuarios
   - **Qué les dice:** Cómo probar la app móvil

**Resumen para App Móvil:**
- Endpoints de autenticación operativa
- Credenciales de prueba
- Sistema de permisos (si lo implementan)

---

## ❌ Documentos que NO Compartir con Frontends

Estos son solo para backend/DBA:

1. **`docs/MIGRACIONES_COMPLETAS.md`**
   - Solo para quien maneja la base de datos
   - Los frontends no necesitan saber sobre migraciones SQL

2. **`src/main/resources/db/migrations/VERIFICACION_TABLAS.sql`**
   - Script SQL técnico
   - Solo para DBA/backend

3. **`src/main/resources/db/migrations/README.md`**
   - Documentación técnica de migraciones
   - Solo para backend/DBA

---

## 📧 Mensaje para Compartir

### Para Web Comercial:

```
Hola equipo Web Comercial,

Les comparto la documentación actualizada del backend:

1. ESTADO_BACKEND_COMPLETO.md - Estado general del backend
2. INSTRUCCIONES_FRONTENDS_BREVE.md - Cambios recientes (nombreEmpresa, reset password, update email, asistente IA)
3. INSTRUCCIONES_ASISTENTE_IA.md - ⚠️ IMPORTANTE: Corrección sobre uso del asistente IA
4. INSTRUCCIONES_UPDATE_EMAIL.md - Cómo implementar cambio de email

Cambios principales:
- Campo nombreEmpresa en registro (opcional)
- Reset de contraseña (token se retorna en respuesta en MVP)
- Actualizar email (endpoint PUT /api/comercial/auth/update-email)
- Asistente IA: NO usar VITE_GEMINI_API_KEY, usar endpoint del backend

Cualquier duda, avisen.
```

### Para WebApp:

```
Hola equipo WebApp,

Les comparto la documentación actualizada del backend:

1. ESTADO_BACKEND_COMPLETO.md - Estado general del backend
2. SOLUCION_PERMISOS_ADMINISTRADOR.md - Problema de permisos resuelto

Cambios principales:
- ✅ Sistema de permisos funcionando correctamente
- ✅ ADMINISTRADOR tiene todos los permisos automáticamente
- ✅ Ya no necesitan el workaround para ADMINISTRADOR sin permisos

El backend ahora maneja correctamente los permisos, pueden eliminar el código de manejo especial.
```

### Para App Móvil:

```
Hola equipo App Móvil,

Les comparto la documentación actualizada del backend:

1. ESTADO_BACKEND_COMPLETO.md - Estado general del backend
2. CREDENCIALES_PRUEBA_APPMOVIL.md - Credenciales de prueba

El backend está listo para integrar. Cualquier duda sobre endpoints, consulten ESTADO_BACKEND_COMPLETO.md.
```

---

## ✅ Checklist Antes de Compartir

- [ ] Verificar que todos los documentos estén actualizados
- [ ] Revisar que las URLs de los endpoints sean correctas
- [ ] Confirmar que los ejemplos de código funcionen
- [ ] Asegurar que no haya información sensible (passwords, etc.)

---

**Última actualización:** 2025-01-XX
