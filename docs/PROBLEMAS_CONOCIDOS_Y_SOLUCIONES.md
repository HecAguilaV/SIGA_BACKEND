# 🐛 Problemas Conocidos y Soluciones

## ⚠️ Problemas Reportados y Soluciones

---

## 1. Error en App Móvil: "Field 'precio' is required"

### Problema
```
Error al crear producto: Illegal input: Field 'precio' is required for type serial name 'com.example.sigaapp.data.model.Product', but it was missing at path: $.producto
```

### Causa
El backend retorna `precioUnitario` pero el modelo de Kotlin en la app móvil espera `precio`.

### Solución (Frontend App Móvil)
**Actualizar el modelo de datos en la app móvil:**

```kotlin
// Modelo actual (INCORRECTO)
@Serializable
data class Product(
    val id: Int,
    val nombre: String,
    val precio: String?  // ❌ INCORRECTO
)

// Modelo correcto (CORRECTO)
@Serializable
data class Product(
    val id: Int,
    val nombre: String,
    val precioUnitario: String?,  // ✅ CORRECTO (coincide con backend)
    val descripcion: String?,
    val categoriaId: Int?,
    val codigoBarras: String?,
    val activo: Boolean,
    val fechaCreacion: String,
    val fechaActualizacion: String
)
```

**O usar alias en el serializer:**
```kotlin
@Serializable
data class Product(
    val id: Int,
    val nombre: String,
    @SerialName("precioUnitario") val precio: String?  // Mapea precioUnitario a precio
)
```

### Formato del Backend
El backend **siempre** retorna:
```json
{
  "success": true,
  "producto": {
    "id": 1,
    "nombre": "Fanta",
    "precioUnitario": "1500",  // ⬅️ Campo correcto
    "descripcion": null,
    "categoriaId": null,
    "codigoBarras": null,
    "activo": true,
    "fechaCreacion": "2025-01-XX...",
    "fechaActualizacion": "2025-01-XX..."
  }
}
```

---

## 2. Productos no se sincronizan entre WebApp y App Móvil

### Problema
- Producto creado desde App Móvil se ve en WebApp ✅
- Producto creado desde WebApp NO se ve en App Móvil ❌
- Después de crear producto desde App Móvil, no se ven productos

### Causas Posibles
1. **Caché en App Móvil:** La app no está refrescando la lista después de crear
2. **Error en parsing:** El error de `precio` puede estar causando que falle el parseo de la respuesta
3. **Filtros incorrectos:** La app puede estar filtrando productos de forma incorrecta

### Solución (Frontend App Móvil)
1. **Refrescar lista después de crear producto:**
   ```kotlin
   // Después de crear producto exitosamente
   if (response.success) {
       // Refrescar lista de productos
       loadProducts()  // Volver a llamar GET /api/saas/productos
   }
   ```

2. **Manejar errores correctamente:**
   ```kotlin
   try {
       val response = createProduct(product)
       if (response.success) {
           loadProducts()  // Refrescar
       }
   } catch (e: Exception) {
       // Mostrar error pero NO limpiar lista existente
       showError("Error: ${e.message}")
   }
   ```

3. **Verificar que el endpoint de listar productos funcione:**
   - Endpoint: `GET /api/saas/productos`
   - Debe retornar TODOS los productos activos (sin filtros)

---

## 3. Asistente no encuentra productos que existen

### Problema
- Producto "Fanta" existe en la base de datos
- Asistente dice "no encontré el producto"
- Producto aparece "sin stock"

### Causa
El asistente busca productos por nombre exacto (case-insensitive) pero:
1. Puede haber problemas con espacios o caracteres especiales
2. El producto existe pero NO tiene stock asignado (esto es normal)
3. El contexto RAG puede no estar incluyendo todos los productos

### Solución (Backend - Ya implementada)
✅ **Actualizado:** El asistente ahora:
- Muestra TODOS los productos (no solo para ADMINISTRADOR)
- Muestra productos SIN stock (con nota de que no tienen stock asignado)
- Busca por nombre con `equals(ignoreCase = true)`

### Solución (Usuario)
**Cuando pidas al asistente actualizar stock, especifica:**
- ✅ Bien: "Añade 10 unidades de Fanta al stock del local Bodega Central"
- ❌ Mal: "Añade 10 Fanta" (falta local)

**El asistente necesita:**
- Nombre del producto (ej: "Fanta")
- Nombre del local (ej: "Bodega Central")
- Cantidad (ej: 10)

---

## 4. Productos aparecen "sin stock"

### Problema
Productos creados aparecen "sin stock" aunque existan.

### Causa
**Esto es NORMAL y CORRECTO:**
- Crear un producto NO crea automáticamente stock
- El stock se debe crear/actualizar por separado
- Un producto puede existir sin tener stock asignado a ningún local

### Solución
**Crear stock después de crear producto:**

1. **Desde WebApp/App Móvil:**
   - Crear producto: `POST /api/saas/productos`
   - Actualizar stock: `PUT /api/saas/stock/{productoId}/{localId}`
   - Body: `{"cantidad": 10, "cantidadMinima": 5}`

2. **Desde Asistente IA:**
   - "Añade 10 unidades de [Producto] al stock del local [Local]"
   - El asistente creará el stock automáticamente

### Endpoint para Actualizar Stock
```
PUT /api/saas/stock/{productoId}/{localId}
Body: {
  "cantidad": 10,
  "cantidadMinima": 5
}
```

**Nota:** Si el stock no existe, se crea. Si existe, se actualiza.

---

## 5. Agregar/Quitar productos desde WebApp

### Solicitud
Agregar funcionalidad para agregar o quitar productos desde WebApp.

### Estado Actual
✅ **Ya existe:**
- `POST /api/saas/productos` - Crear producto
- `DELETE /api/saas/productos/{id}` - Eliminar producto (soft delete)

### Implementación en WebApp
1. **Botón "Agregar Producto"** → Formulario → `POST /api/saas/productos`
2. **Botón "Eliminar" en cada producto** → Confirmar → `DELETE /api/saas/productos/{id}`

**Verificar permisos:**
- Crear: OPERADOR y ADMINISTRADOR pueden
- Eliminar: Solo ADMINISTRADOR puede

---

## ✅ Checklist de Verificación

### Para App Móvil
- [ ] Modelo `Product` usa `precioUnitario` (no `precio`)
- [ ] Refrescar lista de productos después de crear
- [ ] Manejar errores sin limpiar lista existente
- [ ] Verificar que `GET /api/saas/productos` retorna todos los productos

### Para WebApp
- [ ] Implementar botón "Agregar Producto"
- [ ] Implementar botón "Eliminar Producto" (solo ADMINISTRADOR)
- [ ] Verificar permisos antes de mostrar acciones

### Para Asistente IA
- [ ] Especificar nombre de producto Y local al actualizar stock
- [ ] Entender que productos sin stock es normal (se crea por separado)

---

## 📝 Notas Técnicas

### Formato de Respuesta del Backend (Crear Producto)
```json
{
  "success": true,
  "message": "Producto creado exitosamente",
  "producto": {
    "id": 1,
    "nombre": "Fanta",
    "precioUnitario": "1500",  // ⬅️ Campo correcto
    "descripcion": null,
    "categoriaId": null,
    "codigoBarras": null,
    "activo": true,
    "fechaCreacion": "2025-01-XX...",
    "fechaActualizacion": "2025-01-XX..."
  }
}
```

### Flujo Correcto: Crear Producto + Stock
1. Crear producto: `POST /api/saas/productos`
2. Obtener `productoId` de la respuesta
3. Obtener `localId` (del local seleccionado)
4. Crear/actualizar stock: `PUT /api/saas/stock/{productoId}/{localId}`

---

## 6. Error de Parsing JSON en Asistente IA (App Móvil)

### Problema
```
Error: Illegal input: Unexpected JSON token at offset 92: Expected beginning of the string, but got {"succes":true,"response":"X Producto no encontrado: mantequillas",...}
```

### Causa
El frontend está tratando de parsear el campo `response` como JSON cuando es solo texto. Los emojis (✅, ❌) pueden causar problemas de encoding.

### Solución (Backend - Ya implementada)
✅ **Actualizado:** El backend ahora retorna:
- `"Éxito: [mensaje]"` en lugar de `"✅ [mensaje]"`
- `"Error: [mensaje]"` en lugar de `"❌ [mensaje]"`

### Solución (Frontend App Móvil)
**El campo `response` es texto plano, NO JSON:**
```kotlin
// CORRECTO
data class ChatResponse(
    val success: Boolean,
    val response: String,  // ⬅️ Es texto, NO JSON
    val message: String?,
    val action: ActionInfo?
)

// Al parsear:
val chatResponse = json.decodeFromString<ChatResponse>(responseBody)
val mensaje = chatResponse.response  // Ya es String, no necesita parseo adicional
```

### Mejoras en Búsqueda de Productos
✅ **Actualizado:** El asistente ahora:
- Busca productos de forma flexible (coincidencia exacta, contiene, etc.)
- Muestra mensajes de error más claros
- Sugiere listar productos si no encuentra uno

**Ejemplo de uso mejorado:**
- Usuario: "agregar cinco mantequillas al local the House"
- Asistente busca "mantequillas" de forma flexible
- Si no encuentra, sugiere: "No encontré el producto 'mantequillas'. ¿Podrías verificar el nombre exacto? Puedes listar los productos disponibles."

---

**Última actualización:** 2025-01-XX
