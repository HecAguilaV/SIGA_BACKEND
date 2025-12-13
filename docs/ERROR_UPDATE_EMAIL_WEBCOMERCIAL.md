# 🚨 ERROR CRÍTICO: Update Email - WebComercial

## ⚠️ ESTE ES UN PROBLEMA DE WEBCOMERCIAL, NO DE WEBAPP

**Fecha:** 2025-01-XX  
**Prioridad:** 🔴 ALTA  
**Equipo Responsable:** WebComercial

---

## 📋 RESUMEN DEL PROBLEMA

El endpoint `PUT /api/comercial/auth/update-email` está retornando **404 Not Found** desde el frontend de WebComercial.

**IMPORTANTE:** Este endpoint es del **esquema COMERCIAL** (`/api/comercial/auth/*`), NO del operativo. WebApp NO debe tocar esto.

---

## ✅ ESTADO DEL BACKEND

### El Endpoint EXISTE y FUNCIONA

**Endpoint:** `PUT /api/comercial/auth/update-email`  
**Método:** `PUT` (NO POST)  
**Ruta Base:** `/api/comercial/auth`  
**Requiere:** Autenticación (token JWT comercial)

**Verificación del Backend:**
```bash
# El endpoint responde correctamente (sin token retorna "No autenticado")
curl -X PUT https://siga-backend-production.up.railway.app/api/comercial/auth/update-email \
  -H "Content-Type: application/json" \
  -d '{"newEmail":"test@test.com","password":"test"}'

# Respuesta: {"success":false,"message":"No autenticado"}
# ✅ Esto confirma que el endpoint EXISTE
```

---

## 🔍 CAUSA DEL ERROR

El frontend de WebComercial está haciendo una de estas cosas incorrectas:

1. ❌ **Usando POST en lugar de PUT**
2. ❌ **URL incorrecta o mal formada**
3. ❌ **No enviando el token de autenticación**
4. ❌ **Token expirado o inválido**

---

## ✅ SOLUCIÓN PARA WEBCOMERCIAL

### 1. Verificar el Método HTTP

**DEBE SER `PUT`, NO `POST`:**

```javascript
// ❌ INCORRECTO
const response = await fetch('.../update-email', {
  method: 'POST',  // ❌ MAL
  // ...
});

// ✅ CORRECTO
const response = await fetch('https://siga-backend-production.up.railway.app/api/comercial/auth/update-email', {
  method: 'PUT',  // ✅ CORRECTO
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`  // ⬅️ IMPORTANTE
  },
  body: JSON.stringify({
    newEmail: 'nuevo@email.com',
    password: 'contraseñaActual'
  })
});
```

### 2. Verificar la URL Completa

**URL Correcta:**
```
https://siga-backend-production.up.railway.app/api/comercial/auth/update-email
```

**Verificar:**
- ✅ Base URL: `https://siga-backend-production.up.railway.app`
- ✅ Ruta: `/api/comercial/auth/update-email` (NO `/api/auth/update-email`)
- ✅ Método: `PUT` (NO POST)

### 3. Verificar el Token de Autenticación

**El token debe ser:**
- ✅ Token JWT comercial (del login de WebComercial)
- ✅ Enviado en header `Authorization: Bearer <token>`
- ✅ No expirado
- ✅ Válido

**Ejemplo de implementación completa:**

```javascript
async function actualizarEmail(nuevoEmail, contraseñaActual) {
  // 1. Obtener token del localStorage/sessionStorage
  const accessToken = localStorage.getItem('accessToken');
  
  if (!accessToken) {
    throw new Error('No estás autenticado. Por favor, inicia sesión.');
  }
  
  // 2. Hacer la petición con PUT
  const response = await fetch(
    'https://siga-backend-production.up.railway.app/api/comercial/auth/update-email',
    {
      method: 'PUT',  // ⬅️ PUT, no POST
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`  // ⬅️ Token requerido
      },
      body: JSON.stringify({
        newEmail: nuevoEmail,
        password: contraseñaActual
      })
    }
  );
  
  // 3. Manejar respuesta
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error al actualizar email');
  }
  
  const data = await response.json();
  
  // 4. Actualizar tokens si el backend los retorna
  if (data.accessToken && data.refreshToken) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
  }
  
  return data;
}
```

---

## 📝 ESPECIFICACIÓN DEL ENDPOINT

### Request

**Método:** `PUT`  
**URL:** `/api/comercial/auth/update-email`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer <token_jwt_comercial>
```

**Body:**
```json
{
  "newEmail": "nuevo@email.com",
  "password": "contraseñaActual"
}
```

### Response (Éxito - 200)

```json
{
  "success": true,
  "message": "Email actualizado exitosamente",
  "accessToken": "nuevo_token_jwt",
  "refreshToken": "nuevo_refresh_token",
  "user": {
    "id": 1,
    "email": "nuevo@email.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rut": null,
    "telefono": null,
    "nombreEmpresa": "Mi Empresa"
  }
}
```

### Response (Errores)

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "No autenticado"
}
```

**401 Unauthorized (contraseña incorrecta):**
```json
{
  "success": false,
  "message": "Contraseña incorrecta"
}
```

**404 Not Found (usuario no encontrado):**
```json
{
  "success": false,
  "message": "Usuario no encontrado"
}
```

**409 Conflict (email ya en uso):**
```json
{
  "success": false,
  "message": "El email ya está en uso por otro usuario"
}
```

**400 Bad Request (mismo email):**
```json
{
  "success": false,
  "message": "El nuevo email es igual al actual"
}
```

---

## ⚠️ ERRORES COMUNES

### Error 1: "Endpoint no encontrado (404)"

**Causa:** Método HTTP incorrecto (POST en lugar de PUT) o URL mal formada.

**Solución:**
- ✅ Verificar que el método sea `PUT`
- ✅ Verificar que la URL sea exactamente `/api/comercial/auth/update-email`
- ✅ Verificar que la base URL sea correcta

### Error 2: "No autenticado"

**Causa:** Token no enviado, expirado o inválido.

**Solución:**
- ✅ Verificar que el token se envía en header `Authorization: Bearer <token>`
- ✅ Verificar que el token no haya expirado
- ✅ Si expiró, usar `POST /api/comercial/auth/refresh` para renovarlo

### Error 3: "Contraseña incorrecta"

**Causa:** La contraseña enviada no coincide con la contraseña actual del usuario.

**Solución:**
- ✅ Verificar que se está enviando la contraseña actual (no la nueva)
- ✅ Verificar que el usuario ingrese correctamente la contraseña

---

## 🚫 LO QUE WEBAPP NO DEBE HACER

**WebApp NO debe:**
- ❌ Intentar implementar este endpoint (es de WebComercial)
- ❌ Modificar código relacionado con `/api/comercial/auth/*`
- ❌ Asumir que este es su problema

**WebApp tiene sus propios endpoints:**
- ✅ `/api/auth/login` - Login operativo
- ✅ `/api/saas/*` - Gestión operativa

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Verificar que el método HTTP sea `PUT` (no POST)
- [ ] Verificar que la URL sea `/api/comercial/auth/update-email`
- [ ] Verificar que se envía el token en header `Authorization: Bearer <token>`
- [ ] Verificar que el body contiene `newEmail` y `password`
- [ ] Manejar todos los códigos de error (401, 404, 409, 400)
- [ ] Actualizar tokens si el backend los retorna
- [ ] Mostrar mensajes de error claros al usuario
- [ ] Probar con token válido
- [ ] Probar con token expirado (debe mostrar error)
- [ ] Probar con contraseña incorrecta (debe mostrar error)
- [ ] Probar con email ya en uso (debe mostrar error)

---

## 📞 CONTACTO

Si después de verificar todo lo anterior el error persiste, contactar al equipo de backend con:
- URL exacta que se está usando
- Método HTTP usado
- Headers enviados (sin el token por seguridad)
- Código de error exacto
- Mensaje de error del backend

---

## 📚 REFERENCIAS

- **Documento Principal:** `docs/FUENTE_VERDAD_BACKEND.md`
- **Problemas Conocidos:** `docs/PROBLEMAS_CONOCIDOS_Y_SOLUCIONES.md` (Sección 8)
- **Swagger/OpenAPI:** `https://siga-backend-production.up.railway.app/swagger-ui.html`

---

**Última actualización:** 2025-01-XX  
**Equipo:** WebComercial  
**Estado:** 🔴 PENDIENTE DE IMPLEMENTACIÓN
