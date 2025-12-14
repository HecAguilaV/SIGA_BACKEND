# 🐛 Problemas App Móvil Reportados - Soluciones

**Fecha:** 2025-01-XX  
**Equipo:** App Móvil  
**Prioridad:** 🔴 ALTA

---

## 📋 PROBLEMAS REPORTADOS

### 1. ❌ Precios muestran $0

**Síntoma:** Los productos se muestran con precio $0 aunque tengan precio asignado.

**Causa:** El frontend no está parseando correctamente el campo `precioUnitario` del backend.

**Backend retorna:**
```json
{
  "success": true,
  "productos": [
    {
      "id": 1,
      "nombre": "Fanta",
      "precioUnitario": "1500",  // ⬅️ Campo correcto (String)
      "activo": true,
      ...
    }
  ]
}
```

**Solución Frontend:**
1. Verificar que el modelo de datos use `precioUnitario` (no `precio`)
2. Parsear el String a número antes de mostrar
3. Si el precio es `null`, mostrar "Sin precio" o "N/A"

**Ejemplo Kotlin:**
```kotlin
@Serializable
data class Product(
    val id: Int,
    val nombre: String,
    @SerialName("precioUnitario") val precioUnitario: String?,  // ⬅️ Campo correcto
    val activo: Boolean,
    ...
)

// Al mostrar:
val precio = producto.precioUnitario?.toDoubleOrNull() ?: 0.0
textView.text = "$${precio.toInt()}"
```

---

### 2. ✅ Nombres se muestran correctamente

**Estado:** ✅ RESUELTO - Los nombres se muestran correctamente.

---

### 3. ❌ No se pueden eliminar categorías

**Síntoma:** No hay opción para eliminar categorías existentes.

**Backend:**
- ✅ Endpoint existe: `DELETE /api/saas/categorias/{id}`
- ✅ Requiere permiso: `CATEGORIAS_ELIMINAR`
- ✅ ADMINISTRADOR tiene este permiso por defecto

**Solución Frontend:**
1. Agregar botón/acción para eliminar categoría
2. Llamar a `DELETE /api/saas/categorias/{id}`
3. Actualizar lista después de eliminar

**Ejemplo:**
```kotlin
suspend fun eliminarCategoria(id: Int): Result<Unit> {
    val response = httpClient.delete("$baseUrl/api/saas/categorias/$id") {
        header("Authorization", "Bearer $token")
    }
    // Manejar respuesta
}
```

---

### 4. ❌ Al borrar producto, queda "Producto s/n" y el espacio sigue ahí

**Síntoma:** 
- Al borrar un producto, el nombre desaparece pero queda "Producto s/n"
- La fila del producto sigue visible

**Causa:** 
- El backend hace **soft delete** (`activo = false`)
- El frontend está mostrando productos inactivos o no está filtrando correctamente

**Backend:**
- ✅ Solo retorna productos con `activo = true` en `GET /api/saas/productos`
- ✅ Al eliminar, marca `activo = false`

**Solución Frontend:**
1. **NO mostrar productos con `activo = false`**
2. Filtrar en el frontend: `productos.filter { it.activo }`
3. O confiar en que el backend solo retorna activos (ya lo hace)

**Ejemplo:**
```kotlin
// Filtrar productos activos
val productosActivos = productos.filter { it.activo }

// O simplemente usar los que retorna el backend (ya están filtrados)
```

**Nota:** Si el frontend está mostrando "Producto s/n", puede ser que:
- El nombre esté vacío o null en la base de datos
- El frontend tenga un fallback que muestra "Producto s/n" cuando el nombre es null

---

### 5. ❌ Error 429 en Asistente IA

**Síntoma:**
```
Error al procesar con Gemini API: 429 Too Many Request
```

**Causa:** 
- Límite de rate de la API de Gemini excedido
- Demasiadas solicitudes en poco tiempo

**Solución Backend (implementada):**
- ✅ Manejo de error 429 con mensaje amigable
- ✅ Retorna: "Se han realizado demasiadas solicitudes. Por favor, espera unos momentos antes de intentar nuevamente."

**Solución Frontend:**
1. Mostrar mensaje amigable al usuario
2. Deshabilitar botón de enviar por unos segundos
3. Implementar rate limiting en el frontend (esperar 2-3 segundos entre mensajes)

---

## ✅ VERIFICACIONES BACKEND

### Endpoints Funcionando Correctamente:

1. **GET /api/saas/productos**
   - ✅ Solo retorna productos con `activo = true`
   - ✅ Filtra por empresa automáticamente
   - ✅ Retorna `precioUnitario` como String

2. **DELETE /api/saas/productos/{id}**
   - ✅ Marca `activo = false` (soft delete)
   - ✅ Verifica que pertenezca a la empresa del usuario

3. **DELETE /api/saas/categorias/{id}**
   - ✅ Marca `activa = false` (soft delete)
   - ✅ Verifica que pertenezca a la empresa del usuario
   - ✅ Requiere permiso `CATEGORIAS_ELIMINAR` (ADMINISTRADOR lo tiene)

4. **POST /api/saas/chat**
   - ✅ Maneja error 429 con mensaje amigable
   - ✅ Maneja error 503 con mensaje amigable
   - ✅ Maneja timeouts correctamente

---

## 📝 ACCIONES REQUERIDAS FRONTEND

### Prioridad Alta:

1. **Precios $0:**
   - [ ] Verificar modelo de datos usa `precioUnitario`
   - [ ] Parsear String a número correctamente
   - [ ] Manejar caso cuando precio es `null`

2. **Productos eliminados:**
   - [ ] Filtrar productos con `activo = false`
   - [ ] Remover fila de la lista al eliminar
   - [ ] No mostrar "Producto s/n" (verificar que nombre no sea null)

3. **Eliminar categorías:**
   - [ ] Agregar botón/acción para eliminar
   - [ ] Implementar llamada a `DELETE /api/saas/categorias/{id}`
   - [ ] Actualizar lista después de eliminar

### Prioridad Media:

4. **Error 429 Asistente:**
   - [ ] Mostrar mensaje amigable al usuario
   - [ ] Implementar rate limiting (esperar 2-3 segundos entre mensajes)
   - [ ] Deshabilitar botón temporalmente después de error 429

---

## 🔍 DEBUGGING

### Para verificar precios:

```bash
# Ver qué retorna el backend
GET /api/saas/productos

# Verificar en base de datos
SELECT id, nombre, precio_unitario, activo 
FROM siga_saas.PRODUCTOS 
WHERE activo = true;
```

### Para verificar productos eliminados:

```bash
# Ver productos activos (debe retornar solo activos)
GET /api/saas/productos

# Ver productos inactivos (no deberían aparecer)
# El backend NO retorna productos inactivos
```

---

**Última actualización:** 2025-01-XX  
**Estado:** 🔍 REQUIERE ACCIONES DEL FRONTEND
