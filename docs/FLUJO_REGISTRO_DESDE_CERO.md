# 🔄 Flujo de Registro desde Cero - Separación por Empresa

**Fecha:** 2025-01-XX  
**Propósito:** Documentar el flujo completo cuando se limpia la base de datos y se empieza desde cero

---

## 📋 FLUJO COMPLETO

### 1. Registro en Web Comercial

**Endpoint:** `POST /api/comercial/auth/register`

**Qué hace:**
- Crea un `UsuarioComercial` (dueño/empresa)
- NO crea usuario operativo todavía
- Retorna tokens JWT

**Datos creados:**
```sql
-- En siga_comercial.USUARIOS
INSERT INTO siga_comercial.USUARIOS (
    email, password_hash, nombre, apellido, 
    nombre_empresa, activo, fecha_creacion
) VALUES (...);
```

**Estado:** Usuario comercial creado, pero **NO tiene acceso a WebApp todavía**

---

### 2. Crear Suscripción (Comprar Plan)

**Endpoint:** `POST /api/comercial/suscripciones`

**Qué hace:**
1. Crea la suscripción en `siga_comercial.SUSCRIPCIONES`
2. **AUTOMÁTICAMENTE crea usuario operativo** en `siga_saas.USUARIOS` con:
   - `email` = mismo email del usuario comercial
   - `password_hash` = mismo password del usuario comercial
   - `rol` = `ADMINISTRADOR`
   - **`usuario_comercial_id` = ID del usuario comercial** ⬅️ **ASIGNACIÓN AUTOMÁTICA**

**Código relevante:**
```kotlin
// En SuscripcionesController.crearSuscripcion()
if (!usuarioSaasRepository.existsByEmail(usuario.email)) {
    val usuarioOperativo = UsuarioSaas(
        email = usuario.email,
        passwordHash = usuario.passwordHash,
        nombre = usuario.nombre,
        apellido = usuario.apellido,
        rol = Rol.ADMINISTRADOR,
        usuarioComercialId = usuario.id, // ⬅️ ASIGNACIÓN AUTOMÁTICA
        activo = true,
        fechaCreacion = Instant.now(),
        fechaActualizacion = Instant.now()
    )
    usuarioSaasRepository.save(usuarioOperativo)
}
```

**Estado:** 
- ✅ Usuario comercial tiene suscripción
- ✅ Usuario operativo creado con `usuario_comercial_id` asignado
- ✅ Puede hacer login en WebApp con mismo email/password

---

### 3. Login en WebApp (App Móvil)

**Endpoint:** `POST /api/saas/auth/login`

**Qué hace:**
1. Autentica usuario operativo
2. **AUTO-ASIGNA empresa si no tiene** (nuevo desde último fix):
   ```kotlin
   // Si usuario no tiene usuario_comercial_id, lo busca por email
   if (user.usuarioComercialId == null) {
       val usuarioComercial = usuarioComercialRepository.findByEmail(user.email.lowercase())
       if (usuarioComercial != null) {
           user.usuarioComercialId = usuarioComercial.id
           usuarioSaasRepository.save(user)
       }
   }
   ```
3. Retorna tokens JWT

**Estado:** Usuario autenticado con `usuario_comercial_id` garantizado

---

### 4. Crear Productos/Locales/Categorías

**Endpoints:**
- `POST /api/saas/productos`
- `POST /api/saas/locales`
- `POST /api/saas/categorias`

**Qué hace:**
1. Obtiene `usuario_comercial_id` del usuario autenticado
2. **Asigna automáticamente** `usuario_comercial_id` al crear:
   ```kotlin
   val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
   if (usuarioComercialId == null) {
       return ResponseEntity.status(HttpStatus.BAD_REQUEST)
           .body(mapOf("success" to false, 
                       "message" to "No se pudo determinar la empresa"))
   }
   
   val nuevoProducto = Producto(
       nombre = request.nombre,
       precioUnitario = precioUnitario,
       usuarioComercialId = usuarioComercialId, // ⬅️ ASIGNACIÓN AUTOMÁTICA
       activo = true,
       ...
   )
   ```

**Estado:** Todos los datos creados tienen `usuario_comercial_id` asignado

---

## 🎯 PARA APP MÓVIL

### ¿Qué debe hacer App Móvil?

**✅ NADA ESPECIAL** - El backend maneja todo automáticamente.

### Flujo para App Móvil:

1. **Usuario se registra en Web Comercial** (no es responsabilidad de App Móvil)

2. **Usuario compra plan en Web Comercial** (no es responsabilidad de App Móvil)

3. **Usuario hace login en App Móvil:**
   ```kotlin
   // App Móvil llama:
   POST /api/saas/auth/login
   {
     "email": "usuario@empresa.com",
     "password": "password123"
   }
   
   // Backend retorna:
   {
     "success": true,
     "accessToken": "...",
     "refreshToken": "...",
     "user": {
       "id": 1,
       "email": "usuario@empresa.com",
       "rol": "ADMINISTRADOR"
     }
   }
   ```
   
   **Backend automáticamente:**
   - Asigna `usuario_comercial_id` si no tiene
   - Garantiza que el usuario tenga empresa

4. **App Móvil hace requests normales:**
   ```kotlin
   // Listar productos
   GET /api/saas/productos
   // Backend automáticamente filtra por usuario_comercial_id
   
   // Crear producto
   POST /api/saas/productos
   {
     "nombre": "Producto 1",
     "precioUnitario": "1500"
   }
   // Backend automáticamente asigna usuario_comercial_id
   ```

### ⚠️ IMPORTANTE para App Móvil:

1. **Campo de precio:** Usar `precioUnitario` (no `precio`)
   ```kotlin
   data class Product(
       @SerialName("precioUnitario") val precioUnitario: String?
   )
   ```

2. **Filtrar productos eliminados:** Backend solo retorna `activo = true`, pero App Móvil debe recargar después de DELETE

3. **No necesita manejar `usuario_comercial_id`:** El backend lo maneja automáticamente

4. **Si recibe error "No se pudo determinar la empresa":**
   - El usuario no tiene `usuario_comercial_id` asignado
   - **Solución:** Hacer logout y login nuevamente (el login ahora auto-asigna)
   - O llamar a `PUT /api/saas/usuarios/{id}/empresa` (solo admin)

---

## 🗑️ LIMPIEZA DE DATOS

### Script SQL: `016_limpiar_datos_operativos.sql`

**Qué elimina:**
- ❌ Productos
- ❌ Locales
- ❌ Categorías
- ❌ Stock
- ❌ Ventas
- ❌ Usuarios operativos (se recrearán al crear suscripción)

**Qué mantiene:**
- ✅ Usuarios comerciales (dueños)
- ✅ Planes
- ✅ Suscripciones
- ✅ Facturas
- ✅ Pagos

**Ejecutar:**
```sql
\i src/main/resources/db/migrations/016_limpiar_datos_operativos.sql
```

---

## ✅ VERIFICACIÓN POST-LIMPIEZA

1. **Registrar nuevo usuario comercial:**
   ```bash
   POST /api/comercial/auth/register
   {
     "email": "empresa1@test.com",
     "password": "password123",
     "nombre": "Empresa 1",
     "nombreEmpresa": "Mi Empresa"
   }
   ```

2. **Crear suscripción:**
   ```bash
   POST /api/comercial/suscripciones
   {
     "planId": 1,
     "periodo": "MENSUAL"
   }
   ```

3. **Verificar usuario operativo creado:**
   ```sql
   SELECT id, email, usuario_comercial_id, rol
   FROM siga_saas.USUARIOS
   WHERE email = 'empresa1@test.com';
   -- Debe tener usuario_comercial_id asignado
   ```

4. **Hacer login en App Móvil:**
   ```bash
   POST /api/saas/auth/login
   {
     "email": "empresa1@test.com",
     "password": "password123"
   }
   ```

5. **Crear producto:**
   ```bash
   POST /api/saas/productos
   {
     "nombre": "Producto Test",
     "precioUnitario": "1000"
   }
   ```

6. **Verificar producto tiene empresa:**
   ```sql
   SELECT id, nombre, usuario_comercial_id
   FROM siga_saas.PRODUCTOS
   WHERE nombre = 'Producto Test';
   -- Debe tener usuario_comercial_id asignado
   ```

---

## 📝 RESUMEN PARA APP MÓVIL

**✅ NO necesita cambios** - El backend maneja todo automáticamente.

**Solo asegurarse de:**
1. Usar `precioUnitario` (no `precio`)
2. Recargar lista después de DELETE
3. Si recibe error "No se pudo determinar la empresa", hacer logout/login

**El flujo es transparente para App Móvil:**
- Login → Backend asigna empresa automáticamente
- Crear datos → Backend asigna empresa automáticamente
- Listar datos → Backend filtra por empresa automáticamente

---

**Última actualización:** 2025-01-XX  
**Estado:** ✅ LISTO PARA USO
