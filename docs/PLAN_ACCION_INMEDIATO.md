# 🚨 PLAN DE ACCIÓN INMEDIATO - App Móvil

**Fecha:** 2025-01-XX  
**Prioridad:** 🔴 CRÍTICA  
**Deadline:** App Móvil tenía deadline a las 11 AM, ya pasaron 15 horas

---

## 📋 PROBLEMAS REPORTADOS

1. ❌ **Persistencia de usuario fantasma** - Usuario sigue logueado después de logout
2. ❌ **Sin visualización de precios** - Todos muestran $0
3. ❌ **Productos eliminados** - Cambian nombre a "Producto s/n" y la fila no se borra
4. ❌ **Separación por empresa NO funciona** - 2 usuarios comparten mismos locales y productos

---

## 🔍 DIAGNÓSTICO BACKEND

### ✅ Lo que SÍ funciona en el backend:

1. **Filtrado por empresa:**
   - ✅ `GET /api/saas/productos` filtra por `usuarioComercialId`
   - ✅ `GET /api/saas/locales` filtra por `usuarioComercialId`
   - ✅ `GET /api/saas/stock` filtra por `usuarioComercialId`
   - ✅ Solo retorna productos/locales con `activo = true`

2. **Precios:**
   - ✅ Backend retorna `precioUnitario` como String
   - ✅ Campo existe en `ProductoResponse`

3. **Eliminación:**
   - ✅ Backend hace soft delete (`activo = false`)
   - ✅ Solo retorna productos activos

### ⚠️ Posibles problemas:

1. **Si `getUsuarioComercialId()` retorna `null`:**
   - El backend cae al fallback que retorna **TODOS** los productos/locales
   - Esto explicaría por qué los 2 usuarios ven los mismos datos

2. **Datos en base de datos:**
   - Puede haber productos/locales con `usuario_comercial_id = NULL`
   - Puede haber usuarios operativos sin `usuario_comercial_id` asignado

---

## 🛠️ ACCIONES INMEDIATAS

### 1. Ejecutar diagnóstico de base de datos

```bash
cd /Users/hector/Desktop/SIGA_Backend/SIGA_Backend
python3 scripts/diagnostico_separacion_empresa.py
```

**Esto verificará:**
- ✅ Usuarios operativos y su `usuario_comercial_id`
- ✅ Productos y su separación por empresa
- ✅ Locales y su separación por empresa
- ✅ Stock y consistencia
- ✅ Recomendaciones específicas

### 2. Si hay usuarios sin `usuario_comercial_id`:

**Opción A: Asignar manualmente**
```sql
-- Ver usuarios operativos sin empresa
SELECT u.id, u.email, u.usuario_comercial_id
FROM siga_saas.USUARIOS u
WHERE u.usuario_comercial_id IS NULL;

-- Asignar empresa (ejemplo: usuario ID 1 → empresa ID 5)
UPDATE siga_saas.USUARIOS
SET usuario_comercial_id = 5
WHERE id = 1;
```

**Opción B: El backend debería auto-asignar**
- `SecurityUtils.getUsuarioComercialId()` busca por email y actualiza
- Si no funciona, puede ser que no exista usuario comercial con ese email

### 3. Si hay productos/locales sin empresa:

```bash
# Ejecutar migración de asignación
python3 scripts/ejecutar_migraciones_empresa.py
```

O manualmente:
```sql
-- Ver productos sin empresa
SELECT COUNT(*) FROM siga_saas.PRODUCTOS WHERE usuario_comercial_id IS NULL;

-- Si hay, ejecutar migración 015
\i src/main/resources/db/migrations/015_asignar_empresas_datos_existentes.sql
```

### 4. Verificar logs del backend

Buscar en logs:
```
getUsuarioComercialId: no se encontró usuario comercial
getUsuarioComercialId: usuario_comercial_id es null
```

Si aparecen estos mensajes, el problema es que `getUsuarioComercialId()` retorna `null`.

---

## 🎯 SOLUCIONES POR PROBLEMA

### Problema 1: Persistencia de usuario fantasma

**Causa:** Problema del frontend (App Móvil)
- No está limpiando el token/sesión correctamente
- O está usando caché persistente

**Solución Frontend:**
```kotlin
// Al hacer logout, limpiar TODO:
sessionManager.clearAuthOnly()  // Ya lo tienen según ISSUES_BACKEND.md.resolved
// Pero también limpiar caché de productos/locales
inventoryViewModel.clearCache()
```

**Verificación Backend:**
- El backend valida token en cada request
- Si el token es inválido, retorna 401
- Si el frontend no limpia el token, seguirá autenticado

### Problema 2: Precios muestran $0

**Causa:** Frontend busca campo `precio` pero backend retorna `precioUnitario`

**Solución Frontend:**
```kotlin
// ❌ INCORRECTO
data class Product(val precio: String?)

// ✅ CORRECTO
data class Product(
    @SerialName("precioUnitario") val precioUnitario: String?
)
```

**Verificación Backend:**
```bash
# Probar endpoint
curl -H "Authorization: Bearer TOKEN" \
  https://api.siga.com/api/saas/productos

# Debe retornar:
{
  "productos": [
    {
      "precioUnitario": "1500",  // ⬅️ Este campo existe
      ...
    }
  ]
}
```

### Problema 3: Productos eliminados muestran "Producto s/n"

**Causa:** Frontend muestra productos inactivos o tiene caché

**Solución Frontend:**
```kotlin
// Filtrar productos activos
val productosActivos = productos.filter { it.activo }

// O confiar en backend (ya filtra)
// Pero recargar después de DELETE
loadInventory()  // Ya lo tienen según ISSUES_BACKEND.md.resolved
```

**Verificación Backend:**
- Backend solo retorna productos con `activo = true`
- Si el frontend muestra productos eliminados, es caché local

### Problema 4: 2 usuarios comparten mismos datos

**Causa más probable:** `getUsuarioComercialId()` retorna `null` para uno o ambos usuarios

**Diagnóstico:**
1. Ejecutar script de diagnóstico
2. Verificar logs del backend cuando cada usuario hace login
3. Verificar en base de datos que usuarios tengan `usuario_comercial_id`

**Solución inmediata:**
```sql
-- Ver estado actual
SELECT 
    u.id, 
    u.email, 
    u.usuario_comercial_id,
    uc.id as comercial_id,
    uc.email as comercial_email
FROM siga_saas.USUARIOS u
LEFT JOIN siga_comercial.USUARIOS uc ON LOWER(uc.email) = LOWER(u.email)
ORDER BY u.id;

-- Si usuario_comercial_id es NULL pero existe uc.id, asignar:
UPDATE siga_saas.USUARIOS u
SET usuario_comercial_id = uc.id
FROM siga_comercial.USUARIOS uc
WHERE LOWER(uc.email) = LOWER(u.email)
  AND u.usuario_comercial_id IS NULL;
```

---

## 📊 CHECKLIST DE VERIFICACIÓN

### Backend:
- [ ] Ejecutar diagnóstico de base de datos
- [ ] Verificar que usuarios tengan `usuario_comercial_id`
- [ ] Verificar que productos/locales tengan `usuario_comercial_id`
- [ ] Revisar logs del backend para errores de `getUsuarioComercialId()`
- [ ] Probar endpoints con Postman/curl para cada usuario

### Frontend (App Móvil):
- [ ] Cambiar modelo de datos: `precio` → `precioUnitario`
- [ ] Filtrar productos con `activo = false` (o confiar en backend)
- [ ] Limpiar caché después de logout
- [ ] Recargar lista después de DELETE producto
- [ ] Verificar que token se limpia correctamente

---

## 🚀 ORDEN DE EJECUCIÓN (URGENTE)

1. **AHORA:** Ejecutar diagnóstico
   ```bash
   python3 scripts/diagnostico_separacion_empresa.py
   ```

2. **Si hay usuarios sin empresa:** Asignar manualmente o verificar por qué `getUsuarioComercialId()` falla

3. **Si hay datos sin empresa:** Ejecutar migración 015

4. **Verificar endpoints:** Probar con Postman que cada usuario ve solo sus datos

5. **Comunicar a App Móvil:**
   - Cambio de campo: `precio` → `precioUnitario`
   - Backend filtra correctamente (si `usuarioComercialId` no es null)
   - Recargar después de DELETE

---

## 📝 NOTAS IMPORTANTES

1. **El backend funciona SI `usuarioComercialId` no es null**
   - Si es null, cae al fallback que retorna TODOS los datos
   - Esto es intencional para usuarios legacy, pero causa el problema

2. **El problema de separación es CRÍTICO**
   - Si los usuarios no tienen `usuario_comercial_id` asignado, verán todos los datos
   - El diagnóstico mostrará exactamente qué está pasando

3. **Los otros problemas son del frontend**
   - Precios: campo incorrecto
   - Productos eliminados: caché o no filtra
   - Usuario fantasma: no limpia sesión

---

**Última actualización:** 2025-01-XX  
**Estado:** 🔴 REQUIERE ACCIÓN INMEDIATA
