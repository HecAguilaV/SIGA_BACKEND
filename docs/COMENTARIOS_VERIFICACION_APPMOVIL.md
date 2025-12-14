# Comentarios sobre Verificación de Endpoints - App Móvil

**Fecha:** 2025-01-XX  
**Revisión de:** `CHALLA/ANALISIS_ENDPOINTS_Y_SINCRONIZACION.md`

---

## ✅ ANÁLISIS EXCELENTE Y MUY COMPLETO

El análisis realizado por el equipo de App Móvil es **muy riguroso y detallado**. Identificaron correctamente los problemas críticos y propusieron soluciones claras.

---

## 🔴 PROBLEMAS CRÍTICOS CONFIRMADOS

### 1. ❌ `PUT /api/saas/stock/{id}` NO EXISTE

**Confirmación del backend:**
- ✅ **CORRECTO:** El endpoint `PUT /api/saas/stock/{id}` **NO existe** en el backend
- ✅ **CORRECTO:** Solo existe `POST /api/saas/stock` que crea o actualiza según `productoId` + `localId`

**Código del backend:**
```kotlin
@PostMapping
fun actualizarStock(@Valid @RequestBody request: StockRequest): ResponseEntity<Map<String, Any>> {
    // Busca stock existente por productoId + localId
    val stockExistente = stockRepository.findByProductoIdAndLocalId(request.productoId, request.localId)
    
    val stock = if (stockExistente.isPresent) {
        // ACTUALIZA si existe
        stockExistente.get().copy(...)
    } else {
        // CREA si no existe
        Stock(...)
    }
}
```

**Impacto:**
- ❌ Requests a `PUT /api/saas/stock/{id}` terminan en **404/405**
- ❌ El stock **nunca se persiste** en el backend
- ❌ **No hay sincronización** entre App Móvil y WebApp

**Solución requerida:**
- ✅ Cambiar `ApiService.updateStock()` para usar `POST /api/saas/stock`
- ✅ Enviar `productoId` y `localId` en lugar de `id` auto-incremental
- ✅ Usar el formato documentado: `{ "productoId": 1, "localId": 1, "cantidad": 10, "cantidadMinima": 5 }`

---

### 2. ⚠️ Stock NO se crea automáticamente al crear producto

**Confirmación del backend:**
- ✅ **CORRECTO:** El backend **NO crea stock automáticamente** al crear un producto
- ✅ El stock debe crearse explícitamente con `POST /api/saas/stock`

**Código del backend:**
```kotlin
@PostMapping
fun crearProducto(@Valid @RequestBody request: ProductoRequest): ResponseEntity<Map<String, Any>> {
    // Solo crea el producto, NO crea stock
    val nuevoProducto = Producto(...)
    productoRepository.save(nuevoProducto)
    // NO hay llamada a crear stock
}
```

**Impacto:**
- ⚠️ Productos nuevos no tienen stock inicial
- ⚠️ La app muestra placeholders con `id < 0`
- ⚠️ La vista queda "vacía" aunque existan productos

**Opciones de solución:**

**Opción A: Crear stock inicial desde App Móvil (RECOMENDADO)**
```kotlin
// Después de crear producto
fun crearProducto(producto: Producto) {
    apiService.createProduct(producto).onSuccess { productoCreado ->
        // Crear stock inicial para cada local disponible
        locales.forEach { local ->
            apiService.postStock(
                productoId = productoCreado.id,
                localId = local.id,
                cantidad = 0,
                cantidadMinima = 0
            )
        }
    }
}
```

**Opción B: Backend crea stock automáticamente (FUTURO)**
- Requiere modificación en el backend
- Podría crear stock con cantidad 0 para todos los locales de la empresa
- **No implementado actualmente**

**Recomendación:** Implementar Opción A en App Móvil mientras el backend no tenga esta funcionalidad.

---

### 3. ✅ Precios: `precioUnitario` correcto

**Confirmación del backend:**
- ✅ **CORRECTO:** El backend retorna `precioUnitario` (String, puede ser null)
- ✅ **CORRECTO:** NO existe campo `precio`

**Código del backend:**
```kotlin
data class ProductoResponse(
    val precioUnitario: String?,  // ← Campo correcto
    // NO hay campo "precio"
)
```

**Recomendación:**
- ✅ Continuar usando `precioUnitario` en toda la app
- ✅ Auditar pantallas (`DashboardTile`, `SalesScreen`, etc.) para asegurar que solo usen `precioUnitario`
- ✅ Manejar valores `null` con fallback visual ("Sin precio configurado")

---

## ✅ CONFIRMACIONES TÉCNICAS

### 1. `POST /api/saas/stock` acepta ambos formatos

**Confirmación del backend:**
- ✅ **CORRECTO:** El endpoint acepta tanto `camelCase` como `snake_case`

**Código del backend:**
```kotlin
data class StockRequest(
    @JsonProperty("productoId")      // Nombre principal: productoId
    @JsonAlias("producto_id")         // También acepta: producto_id
    val productoId: Int,
    
    @JsonProperty("localId")          // Nombre principal: localId
    @JsonAlias("local_id")            // También acepta: local_id
    val localId: Int,
    
    @JsonProperty("cantidadMinima")   // Nombre principal: cantidadMinima
    @JsonAlias("cantidad_minima", "min_stock")  // También acepta: cantidad_minima o min_stock
    val cantidadMinima: Int = 0
)
```

**Recomendación:**
- ✅ Usar `camelCase` (formato preferido): `{ "productoId": 1, "localId": 1, "cantidad": 10, "cantidadMinima": 5 }`
- ✅ El backend aceptará ambos formatos, pero `camelCase` es más consistente con el resto de la API

---

### 2. Filtrado por empresa

**Confirmación del backend:**
- ✅ **CORRECTO:** Todo se filtra automáticamente por `usuario_comercial_id`
- ✅ El token JWT debe incluir `usuario_comercial_id` (se asigna automáticamente en login)

**Código del backend:**
```kotlin
@GetMapping
fun listarStock(@RequestParam(required = false) localId: Int?): ResponseEntity<Map<String, Any>> {
    val usuarioComercialId = SecurityUtils.getUsuarioComercialId()
    val stockList = if (usuarioComercialId != null) {
        stockRepository.findByUsuarioComercialId(usuarioComercialId)
    } else {
        // Fallback para usuarios legacy
        stockRepository.findAll()
    }
}
```

**Recomendación:**
- ✅ Verificar que el token JWT tenga `usuario_comercial_id` después del login
- ✅ Si el stock viene vacío, verificar:
  1. Que el usuario tenga empresa asignada
  2. Que el token incluya `usuario_comercial_id`
  3. Que los productos y locales tengan `usuario_comercial_id` asignado

---

### 3. Sincronización automática

**Confirmación:**
- ✅ **CORRECTO:** App Móvil y WebApp usan los mismos endpoints
- ✅ La sincronización es automática cuando se usan los endpoints correctos

**Problema actual:**
- ❌ App Móvil usa `PUT /api/saas/stock/{id}` (no existe) → **NO sincroniza**
- ✅ WebApp usa `POST /api/saas/stock` (correcto) → **Sincroniza**

**Solución:**
- ✅ Cambiar App Móvil para usar `POST /api/saas/stock` → **Sincronización automática restaurada**

---

## 📋 RESPUESTAS A SOLICITUDES DEL EQUIPO

### 1. ¿El backend crea stock automáticamente al crear producto?

**Respuesta:** ❌ **NO**. El backend NO crea stock automáticamente al crear un producto.

**Recomendación:**
- Implementar creación de stock inicial desde App Móvil después de crear producto
- O solicitar al backend que implemente esta funcionalidad en el futuro

---

### 2. ¿`POST /api/saas/stock` acepta ambos formatos?

**Respuesta:** ✅ **SÍ**. El endpoint acepta tanto `camelCase` como `snake_case`:
- `camelCase`: `{ "productoId": 1, "localId": 1, "cantidad": 10, "cantidadMinima": 5 }`
- `snake_case`: `{ "producto_id": 1, "local_id": 1, "cantidad": 10, "cantidad_minima": 5 }` o `{ "min_stock": 5 }`

**Recomendación:** Usar `camelCase` (formato preferido).

---

### 3. ¿Existe `PUT /api/saas/stock/{id}`?

**Respuesta:** ❌ **NO**. Este endpoint NO existe y nunca existió.

**Recomendación:**
- Cambiar inmediatamente a `POST /api/saas/stock`
- El backend podría devolver un error más claro (405 Method Not Allowed) si se intenta PUT, pero actualmente devuelve 404

---

## ✅ PLAN DE ACCIÓN RECOMENDADO

### Prioridad 1: CRÍTICO (Bloquea funcionalidad)

1. **Cambiar endpoint de stock**
   - ❌ Eliminar: `PUT /api/saas/stock/{id}`
   - ✅ Implementar: `POST /api/saas/stock` con `productoId` y `localId`
   - ✅ Actualizar: `ApiService.updateStock()` → `postStock(StockUpdatePayload)`
   - ✅ Actualizar: `SaaSRepository.updateStock()` para enviar `productoId` y `localId`
   - ✅ Actualizar: `InventoryViewModel.updateStock()` para pasar `productoId` y `localId`

### Prioridad 2: IMPORTANTE (Mejora UX)

2. **Crear stock inicial**
   - ✅ Después de `createProduct`, llamar automáticamente a `POST /api/saas/stock` con cantidad 0 para cada local
   - ✅ Eliminar placeholders con `id < 0` una vez que el stock se cree correctamente

3. **Verificar precios**
   - ✅ Auditar todas las pantallas para usar solo `precioUnitario`
   - ✅ Agregar fallback visual para valores `null`

### Prioridad 3: VALIDACIÓN (Asegurar calidad)

4. **Validar filtros por empresa**
   - ✅ Verificar que el token JWT tenga `usuario_comercial_id` después del login
   - ✅ Agregar logs temporales para detectar respuestas vacías
   - ✅ Validar que productos y locales tengan `usuario_comercial_id` asignado

5. **Pruebas end-to-end**
   - ✅ Crear producto → crear/actualizar stock → verificar en Postman, WebApp y App
   - ✅ Ajustar stock en web → refrescar App
   - ✅ Crear stock con `cantidadMinima` distinta → verificar que la app lo muestra

---

## 🎯 CONCLUSIÓN

**El análisis del equipo de App Móvil es excelente y muy completo.**

**Problemas identificados:**
- ✅ **CRÍTICO:** `PUT /api/saas/stock/{id}` no existe → Cambiar a `POST /api/saas/stock`
- ⚠️ **IMPORTANTE:** Stock no se crea automáticamente → Implementar creación desde App Móvil
- ✅ **MENOR:** Verificar uso de `precioUnitario` en todas las pantallas

**Estado del backend:**
- ✅ Endpoints documentados son correctos
- ✅ `POST /api/saas/stock` acepta ambos formatos
- ❌ NO crea stock automáticamente al crear producto
- ❌ NO existe `PUT /api/saas/stock/{id}`

**Recomendación final:**
1. **URGENTE:** Cambiar endpoint de stock a `POST /api/saas/stock` (restaura sincronización)
2. **IMPORTANTE:** Implementar creación de stock inicial desde App Móvil
3. **VALIDACIÓN:** Auditar uso de `precioUnitario` en todas las pantallas

**Calificación del análisis:** ⭐⭐⭐⭐⭐ (5/5)

---

**Última actualización:** 2025-01-XX  
**Revisado por:** Backend SIGA
