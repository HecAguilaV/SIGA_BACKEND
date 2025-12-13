# ⚠️ INSTRUCCIONES CRÍTICAS: Permisos ADMINISTRADOR - WebApp

## 🚨 PROBLEMA RESUELTO EN EL BACKEND

**El backend YA maneja los permisos de ADMINISTRADOR automáticamente. NO necesitas hacer nada especial en el frontend.**

---

## ✅ QUÉ HACE EL BACKEND

### 1. ADMINISTRADOR tiene TODOS los permisos automáticamente

El backend verifica **ANTES** de consultar la base de datos:

```kotlin
// En SecurityUtils.kt
fun tienePermiso(codigoPermiso: String): Boolean {
    // ⬇️ PRIMERO verifica si es ADMINISTRADOR
    if (isAdmin()) {
        return true  // ✅ Retorna true inmediatamente, sin consultar BD
    }
    // Solo si NO es ADMINISTRADOR, consulta permisos en BD
    // ...
}
```

### 2. Todos los endpoints funcionan para ADMINISTRADOR

- ✅ `GET /api/saas/productos` → Funciona
- ✅ `POST /api/saas/productos` → Funciona
- ✅ `GET /api/saas/locales` → Funciona
- ✅ `POST /api/saas/locales` → Funciona
- ✅ `GET /api/saas/usuarios/{id}/permisos` → Funciona
- ✅ **TODO funciona para ADMINISTRADOR**

### 3. El backend retorna permisos completos para ADMINISTRADOR

```kotlin
// En PermisosService.kt
fun obtenerPermisosUsuario(usuarioId: Int): List<String> {
    val usuario = usuarioSaasRepository.findById(usuarioId).orElse(null)
    
    // ⬇️ Si es ADMINISTRADOR, retorna TODOS los permisos
    if (usuario.rol == Rol.ADMINISTRADOR) {
        return permisosRepository.findByActivoTrue().map { it.codigo }
    }
    // Solo si NO es ADMINISTRADOR, consulta permisos del rol
    // ...
}
```

---

## ❌ QUÉ NO DEBES HACER EN EL FRONTEND

### ❌ NO hacer validaciones especiales para ADMINISTRADOR

```javascript
// ❌ INCORRECTO - NO hacer esto
if (user.rol === 'ADMINISTRADOR') {
    // Asumir que tiene todos los permisos
    return true;
}
```

**¿Por qué?** El backend ya lo maneja. Si haces esto, estás duplicando lógica y puede causar inconsistencias.

### ❌ NO hacer workarounds cuando falla obtener permisos

```javascript
// ❌ INCORRECTO - NO hacer esto
try {
    const permisos = await obtenerPermisos(userId);
} catch (error) {
    // Si es ADMINISTRADOR, asumir que tiene todos
    if (user.rol === 'ADMINISTRADOR') {
        permisos = ['TODOS_LOS_PERMISOS'];
    }
}
```

**¿Por qué?** El endpoint `GET /api/saas/usuarios/{id}/permisos` **SÍ funciona** para ADMINISTRADOR y retorna todos los permisos.

### ❌ NO ocultar/mostrar botones basado en rol en lugar de permisos

```javascript
// ❌ INCORRECTO - NO hacer esto
{user.rol === 'ADMINISTRADOR' && <button>Crear Producto</button>}
```

**¿Por qué?** Debes usar permisos, no roles. El backend retorna los permisos correctos.

---

## ✅ QUÉ DEBES HACER EN EL FRONTEND

### 1. Obtener permisos normalmente

```javascript
// ✅ CORRECTO
const obtenerPermisos = async (userId) => {
    const response = await fetch(`/api/saas/usuarios/${userId}/permisos`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    if (!response.ok) {
        throw new Error('Error al obtener permisos');
    }
    
    const data = await response.json();
    return data.permisos; // Array de strings: ['PRODUCTOS_CREAR', 'STOCK_ACTUALIZAR', ...]
};

// Para ADMINISTRADOR, esto retornará TODOS los permisos disponibles
// Para OPERADOR/CAJERO, retornará solo los permisos asignados
```

### 2. Validar permisos antes de mostrar acciones

```javascript
// ✅ CORRECTO
const puedeCrearProductos = permisos.includes('PRODUCTOS_CREAR');

return (
    <div>
        {puedeCrearProductos && (
            <button onClick={crearProducto}>Crear Producto</button>
        )}
    </div>
);
```

### 3. Si el endpoint de permisos falla, mostrar error genérico

```javascript
// ✅ CORRECTO
try {
    const permisos = await obtenerPermisos(userId);
    // Usar permisos normalmente
} catch (error) {
    // Mostrar error genérico, NO hacer workarounds
    console.error('Error al obtener permisos:', error);
    mostrarError('No se pudieron cargar los permisos. Por favor, recarga la página.');
}
```

---

## 🔍 CÓMO VERIFICAR QUE FUNCIONA

### 1. Probar con usuario ADMINISTRADOR

```bash
# Login como ADMINISTRADOR
curl -X POST https://siga-backend-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.test@siga.com","password":"tu_password"}'

# Obtener permisos (debe retornar TODOS los permisos)
curl -X GET https://siga-backend-production.up.railway.app/api/saas/usuarios/18/permisos \
  -H "Authorization: Bearer <token>"

# Debe retornar algo como:
{
  "success": true,
  "permisos": [
    "PRODUCTOS_VER",
    "PRODUCTOS_CREAR",
    "PRODUCTOS_ACTUALIZAR",
    "PRODUCTOS_ELIMINAR",
    "STOCK_VER",
    "STOCK_ACTUALIZAR",
    // ... todos los permisos
  ]
}
```

### 2. Probar crear un local (debe funcionar)

```bash
curl -X POST https://siga-backend-production.up.railway.app/api/saas/locales \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Nuevo Local","direccion":"Calle 123"}'

# Debe retornar 201 Created, NO 403 Forbidden
```

---

## 📋 CHECKLIST PARA EL FRONTEND

- [ ] **NO** hay validaciones especiales para `rol === 'ADMINISTRADOR'`
- [ ] **NO** hay workarounds cuando falla obtener permisos
- [ ] Se usa `GET /api/saas/usuarios/{id}/permisos` normalmente
- [ ] Se validan permisos antes de mostrar acciones: `permisos.includes('PERMISO_X')`
- [ ] Si el endpoint de permisos falla, se muestra error genérico (no workarounds)
- [ ] Los botones/acciones se muestran basados en permisos, no en roles

---

## 🐛 SI SIGUE FALLANDO

### Verificar que el backend está actualizado

El fix está en `SecurityUtils.kt` línea 47-51:

```kotlin
fun tienePermiso(codigoPermiso: String): Boolean {
    // Si es ADMINISTRADOR, tiene todos los permisos automáticamente
    if (isAdmin()) {
        return true  // ⬅️ Esta línea debe existir
    }
    // ...
}
```

### Verificar que la migración de permisos está ejecutada

La tabla `siga_saas.PERMISOS` debe existir. Si no existe, ejecutar:

```sql
-- Verificar que existe
SELECT * FROM siga_saas.PERMISOS LIMIT 5;
```

Si no existe, ejecutar la migración `008_create_sistema_permisos.sql`.

---

## 📞 CONTACTO

Si después de seguir estas instrucciones sigue fallando, **NO implementes workarounds**. Contacta al equipo de backend con:

1. El endpoint que falla
2. El código de error (403, 404, etc.)
3. El rol del usuario
4. Los logs del backend (si están disponibles)

---

**Última actualización:** 2025-01-XX  
**Versión del backend:** Ya implementado y funcionando
