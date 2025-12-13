# ✅ Solución: Error de Serialización en Stock - App Móvil

**Fecha:** 2025-01-XX  
**Prioridad:** 🔴 ALTA  
**Estado:** ✅ RESUELTO EN BACKEND

---

## 📋 RESUMEN

El problema de serialización en el endpoint de stock ha sido **corregido en el backend**. El backend ahora retorna todos los campos requeridos en el formato correcto (snake_case).

---

## ✅ CAMBIOS EN EL BACKEND

### Antes (camelCase - ❌ Incorrecto)
```json
{
  "productoId": 1,
  "localId": 1,
  "cantidadMinima": 10
}
```

### Ahora (snake_case - ✅ Correcto)
```json
{
  "producto_id": 1,
  "local_id": 1,
  "min_stock": 10,
  "fecha_actualizacion": "2025-01-XX..."
}
```

---

## 🔧 ACCIONES PARA APP MÓVIL

### 1. Remover el Parche Temporal

**Ya no es necesario permitir campos nulos.** El backend ahora siempre retorna todos los campos requeridos.

### 2. Actualizar el Modelo de Datos

**Modelo Correcto (sin campos opcionales):**

```kotlin
@Serializable
data class StockItem(
    val id: Int,
    val producto_id: Int,          // ✅ Siempre presente
    val local_id: Int,              // ✅ Siempre presente
    val cantidad: Int,
    val min_stock: Int,             // ✅ Siempre presente (antes cantidadMinima)
    val fecha_actualizacion: String
)
```

**Si tenían un modelo con campos opcionales, ahora pueden hacerlos requeridos:**

```kotlin
// ❌ ANTES (con parche temporal)
@Serializable
data class StockItem(
    val id: Int,
    val producto_id: Int? = null,  // ❌ Opcional por parche
    val local_id: Int? = null,      // ❌ Opcional por parche
    val cantidad: Int,
    val min_stock: Int? = null,     // ❌ Opcional por parche
    val fecha_actualizacion: String
)

// ✅ AHORA (sin parche)
@Serializable
data class StockItem(
    val id: Int,
    val producto_id: Int,          // ✅ Requerido
    val local_id: Int,              // ✅ Requerido
    val cantidad: Int,
    val min_stock: Int,             // ✅ Requerido
    val fecha_actualizacion: String
)
```

### 3. Remover Lógica de Validación Temporal

Si tenían código que validaba campos nulos o mostraba mensajes de error, pueden removerlo:

```kotlin
// ❌ REMOVER este tipo de validaciones
if (stockItem.producto_id == null || stockItem.local_id == null) {
    // Manejo de error temporal
}

// ✅ Ahora siempre estarán presentes
```

---

## 📡 FORMATO DE RESPUESTA DEL BACKEND

### GET /api/saas/stock

**Respuesta (200 OK):**
```json
{
  "success": true,
  "stock": [
    {
      "id": 1,
      "producto_id": 1,
      "local_id": 1,
      "cantidad": 100,
      "min_stock": 10,
      "fecha_actualizacion": "2025-01-13T10:00:00Z"
    },
    {
      "id": 2,
      "producto_id": 2,
      "local_id": 1,
      "cantidad": 50,
      "min_stock": 5,
      "fecha_actualizacion": "2025-01-13T11:00:00Z"
    }
  ],
  "total": 2
}
```

### GET /api/saas/stock/{productoId}/{localId}

**Respuesta (200 OK):**
```json
{
  "success": true,
  "stock": {
    "id": 1,
    "producto_id": 1,
    "local_id": 1,
    "cantidad": 100,
    "min_stock": 10,
    "fecha_actualizacion": "2025-01-13T10:00:00Z"
  }
}
```

### POST /api/saas/stock

**Request Body (acepta snake_case):**
```json
{
  "producto_id": 1,
  "local_id": 1,
  "cantidad": 150,
  "cantidad_minima": 15
}
```

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Stock actualizado exitosamente",
  "stock": {
    "id": 1,
    "producto_id": 1,
    "local_id": 1,
    "cantidad": 150,
    "min_stock": 15,
    "fecha_actualizacion": "2025-01-13T12:00:00Z"
  }
}
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [ ] Remover parche temporal de campos opcionales
- [ ] Actualizar modelo `StockItem` con campos requeridos
- [ ] Verificar que `producto_id` está presente
- [ ] Verificar que `local_id` está presente
- [ ] Verificar que `min_stock` está presente (no `cantidadMinima`)
- [ ] Remover validaciones de campos nulos
- [ ] Probar listar stock: `GET /api/saas/stock`
- [ ] Probar obtener stock específico: `GET /api/saas/stock/{productoId}/{localId}`
- [ ] Probar actualizar stock: `POST /api/saas/stock`
- [ ] Verificar que el filtrado por local funciona correctamente
- [ ] Verificar que la edición de stock funciona correctamente

---

## 🧪 PRUEBAS RECOMENDADAS

1. **Listar todo el stock:**
   ```kotlin
   val response = apiClient.get("/api/saas/stock")
   val stockList = response.stock as List<StockItem>
   // Verificar que todos los items tienen producto_id, local_id, min_stock
   ```

2. **Filtrar por local:**
   ```kotlin
   val response = apiClient.get("/api/saas/stock?localId=1")
   // Verificar que se puede filtrar correctamente usando local_id
   ```

3. **Editar stock:**
   ```kotlin
   val stockItem = stockList.first()
   // Usar stockItem.producto_id y stockItem.local_id para editar
   apiClient.post("/api/saas/stock", UpdateStockRequest(
       producto_id = stockItem.producto_id,
       local_id = stockItem.local_id,
       cantidad = 200
   ))
   ```

---

## 📞 CONTACTO

Si después de aplicar estos cambios el error persiste, contactar al equipo de backend con:
- URL exacta del endpoint
- Respuesta completa del backend (JSON)
- Código del modelo `StockItem` actual
- Logs de error completos

---

## 📚 REFERENCIAS

- **Documento Principal:** `docs/FUENTE_VERDAD_BACKEND.md`
- **Problemas Conocidos:** `docs/PROBLEMAS_CONOCIDOS_Y_SOLUCIONES.md` (Sección 10)
- **Swagger/OpenAPI:** `https://siga-backend-production.up.railway.app/swagger-ui.html`

---

**Última actualización:** 2025-01-XX  
**Equipo:** App Móvil  
**Estado:** ✅ BACKEND CORREGIDO - PENDIENTE DE ACTUALIZACIÓN EN APP
