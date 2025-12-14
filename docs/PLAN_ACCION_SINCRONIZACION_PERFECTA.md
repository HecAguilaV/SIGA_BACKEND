# Plan de Acción: Sincronización Perfecta App Móvil ↔ WebApp

**Fecha:** 2025-01-XX  
**Objetivo:** Garantizar sincronización perfecta en tiempo real entre App Móvil y WebApp  
**Estado:** 🟡 En progreso

---

## 🎯 RESUMEN EJECUTIVO

**Problema principal:** App Móvil usa `PUT /api/saas/stock/{id}` que no existe, bloqueando la sincronización.

**Solución:** Corregir endpoint de stock en App Móvil para usar `POST /api/saas/stock` (igual que WebApp).

**Resultado esperado:** Sincronización automática en tiempo real una vez corregido.

---

## 📊 ESTADO ACTUAL

### Comparación WebApp vs App Móvil

| Aspecto | WebApp | App Móvil | Estado |
|---------|--------|-----------|--------|
| **Endpoint Stock Update** | ✅ `POST /api/saas/stock` | ❌ `PUT /api/saas/stock/{id}` | 🔴 **DESALINEADO** |
| **Campo Precio** | ✅ `precioUnitario` | ⚠️ Necesita auditoría | 🟡 Parcial |
| **Stock Inicial** | ✅ Muestra `stockActual: 0` | ⚠️ Usa placeholders `id < 0` | 🟡 Diferente |
| **Refresh Automático** | ✅ Sí | ⚠️ Necesita verificación | 🟡 Parcial |
| **Sincronización** | ✅ Funciona | ❌ Bloqueada | 🔴 **CRÍTICO** |

---

## 🔴 PROBLEMA CRÍTICO: Endpoint de Stock

### Situación Actual

**App Móvil:**
```kotlin
// ❌ INCORRECTO - Endpoint no existe
PUT /api/saas/stock/{id}
{
  "cantidad": 10
}
```
- ❌ Retorna 404/405
- ❌ Stock nunca se persiste
- ❌ No hay sincronización

**WebApp:**
```javascript
// ✅ CORRECTO - Funciona según documentación
POST /api/saas/stock
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 10,
  "cantidadMinima": 5
}
```
- ✅ Funciona correctamente
- ✅ Stock se persiste
- ✅ Sincronización activa

### Impacto

1. **App Móvil no puede actualizar stock** → Funcionalidad bloqueada
2. **WebApp sí puede actualizar stock** → Funcionalidad operativa
3. **Desincronización total** → Cambios desde WebApp se ven, desde App Móvil no
4. **Experiencia de usuario inconsistente** → Depende de qué app uses

---

## ✅ PLAN DE ACCIÓN

### FASE 1: CORRECCIÓN CRÍTICA (Prioridad ALTA) 🔴

**Objetivo:** Restaurar funcionalidad de stock en App Móvil y habilitar sincronización.

#### 1.1. Cambiar Endpoint de Stock en App Móvil

**Archivos a modificar:**
- `ApiService.updateStock()` → Cambiar a `postStock()`
- `SaaSRepository.updateStock()` → Actualizar para enviar `productoId` y `localId`
- `InventoryViewModel.updateStock()` → Pasar `productoId` y `localId` en lugar de `id`

**Cambio requerido:**
```kotlin
// ❌ ANTES (incorrecto)
PUT /api/saas/stock/{id}
{
  "cantidad": 10
}

// ✅ DESPUÉS (correcto)
POST /api/saas/stock
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 10,
  "cantidadMinima": 5
}
```

**Formato a usar:**
- ✅ Usar `camelCase` (formato preferido): `productoId`, `localId`, `cantidadMinima`
- ✅ El backend acepta ambos formatos, pero `camelCase` es más consistente

**Tiempo estimado:** 2-4 horas  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🔴 CRÍTICA

---

#### 1.2. Verificar y Corregir Mapeo de Stock

**Verificar:**
- Que `productoId` y `localId` estén disponibles en el contexto donde se actualiza stock
- Que el mapeo de respuesta del backend sea correcto

**Backend retorna:**
```json
{
  "success": true,
  "message": "Stock actualizado exitosamente",
  "stock": {
    "id": 1,
    "producto_id": 1,
    "local_id": 1,
    "cantidad": 10,
    "min_stock": 5,
    "fecha_actualizacion": "..."
  }
}
```

**Nota:** El backend retorna `snake_case` en la respuesta, pero acepta `camelCase` en el request.

**Tiempo estimado:** 1-2 horas  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🔴 CRÍTICA

---

#### 1.3. Implementar Refresh Automático

**Verificar:**
- Que después de actualizar stock, se recargue la lista automáticamente
- Que el refresh funcione igual que en WebApp

**Implementación sugerida:**
```kotlin
fun updateStock(productoId: Int, localId: Int, cantidad: Int) {
    apiService.postStock(productoId, localId, cantidad).onSuccess {
        // Recargar lista de stock automáticamente
        loadInventory()
    }
}
```

**Tiempo estimado:** 1 hora  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🔴 CRÍTICA

---

### FASE 2: MEJORAS IMPORTANTES (Prioridad MEDIA) 🟡

**Objetivo:** Mejorar experiencia de usuario y consistencia con WebApp.

#### 2.1. Manejo de Stock Inicial

**Problema actual:**
- App Móvil usa placeholders con `id < 0` cuando no hay stock
- WebApp muestra `stockActual: 0` (más claro)

**Solución recomendada:**

**Opción A: Crear stock inicial automáticamente (RECOMENDADO)**
```kotlin
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

**Opción B: Mostrar 0 sin placeholders (ALTERNATIVA)**
- Eliminar lógica de placeholders con `id < 0`
- Mostrar `stockActual: 0` cuando no hay registro de stock
- Igual que WebApp

**Recomendación:** Implementar Opción A (crear stock inicial automáticamente)

**Tiempo estimado:** 2-3 horas  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🟡 MEDIA

---

#### 2.2. Auditoría de Campo Precio

**Verificar:**
- Que todas las pantallas usen `precioUnitario` (no `precio`)
- Pantallas a auditar:
  - `DashboardTile`
  - `SalesScreen`
  - `ProductDetailScreen`
  - `InventoryScreen`
  - Cualquier otra que muestre precios

**Implementar fallback visual:**
```kotlin
fun getPrecioDisplay(producto: Producto): String {
    return producto.precioUnitario?.let { 
        "$$it" 
    } ?: "Sin precio configurado"
}
```

**Tiempo estimado:** 2-3 horas  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🟡 MEDIA

---

### FASE 3: VALIDACIÓN Y PRUEBAS (Prioridad MEDIA) 🟡

**Objetivo:** Asegurar que la sincronización funcione perfectamente.

#### 3.1. Pruebas End-to-End

**Escenarios a validar:**

1. **Crear producto en App Móvil → Ver en WebApp**
   - Crear producto en App Móvil
   - Verificar que aparece en WebApp (refresh)
   - Tiempo esperado: < 5 segundos

2. **Actualizar stock en App Móvil → Ver en WebApp**
   - Actualizar stock en App Móvil
   - Verificar que se refleja en WebApp (refresh)
   - Tiempo esperado: < 5 segundos

3. **Actualizar stock en WebApp → Ver en App Móvil**
   - Actualizar stock en WebApp
   - Verificar que se refleja en App Móvil (refresh)
   - Tiempo esperado: < 5 segundos

4. **Crear venta en App Móvil → Ver en WebApp**
   - Crear venta en App Móvil
   - Verificar que aparece en WebApp
   - Verificar que el stock se actualiza correctamente

5. **Stock con cantidadMinima**
   - Crear stock con `cantidadMinima` distinta
   - Verificar que ambas apps lo muestran correctamente

**Tiempo estimado:** 4-6 horas  
**Responsable:** Equipos App Móvil y WebApp  
**Prioridad:** 🟡 MEDIA

---

#### 3.2. Validación de Filtros por Empresa

**Verificar:**
- Que el token JWT tenga `usuario_comercial_id` después del login
- Que los productos y locales tengan `usuario_comercial_id` asignado
- Que el stock se filtre correctamente por empresa

**Logs temporales sugeridos:**
```kotlin
fun loadInventory() {
    apiService.getStock().onSuccess { stock ->
        Log.d("Stock", "Stock recibido: ${stock.size} items")
        Log.d("Stock", "Usuario comercial ID: ${getUsuarioComercialId()}")
    }
}
```

**Tiempo estimado:** 1-2 horas  
**Responsable:** Equipo App Móvil  
**Prioridad:** 🟡 MEDIA

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Fase 1: Corrección Crítica 🔴

- [ ] Cambiar `ApiService.updateStock()` a `postStock()` con `productoId` y `localId`
- [ ] Actualizar `SaaSRepository.updateStock()` para enviar payload correcto
- [ ] Actualizar `InventoryViewModel.updateStock()` para pasar `productoId` y `localId`
- [ ] Verificar mapeo de respuesta del backend
- [ ] Implementar refresh automático después de actualizar stock
- [ ] Probar que el stock se actualiza correctamente
- [ ] Verificar que los cambios se reflejan en WebApp

### Fase 2: Mejoras Importantes 🟡

- [ ] Implementar creación de stock inicial al crear producto
- [ ] Eliminar placeholders con `id < 0`
- [ ] Auditar todas las pantallas para usar `precioUnitario`
- [ ] Implementar fallback visual para precios `null`
- [ ] Verificar consistencia con WebApp

### Fase 3: Validación y Pruebas 🟡

- [ ] Probar escenario: Crear producto App Móvil → Ver en WebApp
- [ ] Probar escenario: Actualizar stock App Móvil → Ver en WebApp
- [ ] Probar escenario: Actualizar stock WebApp → Ver en App Móvil
- [ ] Probar escenario: Crear venta App Móvil → Ver en WebApp
- [ ] Probar stock con `cantidadMinima` distinta
- [ ] Validar filtros por empresa
- [ ] Verificar sincronización en tiempo real (< 5 segundos)

---

## 🎯 RESULTADO ESPERADO

### Después de la Fase 1 (Corrección Crítica)

✅ App Móvil puede actualizar stock correctamente  
✅ Sincronización automática restaurada  
✅ Cambios desde App Móvil se reflejan en WebApp  
✅ Cambios desde WebApp se reflejan en App Móvil  
✅ Experiencia de usuario consistente

### Después de la Fase 2 (Mejoras Importantes)

✅ Stock inicial creado automáticamente  
✅ Sin placeholders con `id < 0`  
✅ Precios consistentes en todas las pantallas  
✅ Manejo correcto de valores `null`

### Después de la Fase 3 (Validación)

✅ Sincronización perfecta verificada  
✅ Todos los escenarios funcionando  
✅ Filtros por empresa validados  
✅ Documentación actualizada

---

## 📊 CRONOGRAMA SUGERIDO

### Semana 1: Fase 1 (Corrección Crítica)
- **Día 1-2:** Cambiar endpoint de stock
- **Día 3:** Verificar mapeo y refresh automático
- **Día 4-5:** Pruebas básicas de sincronización

### Semana 2: Fase 2 (Mejoras Importantes)
- **Día 1-2:** Implementar creación de stock inicial
- **Día 3:** Auditar y corregir uso de precios
- **Día 4-5:** Eliminar placeholders y mejorar UX

### Semana 3: Fase 3 (Validación)
- **Día 1-3:** Pruebas end-to-end completas
- **Día 4:** Validación de filtros por empresa
- **Día 5:** Documentación final y cierre

**Tiempo total estimado:** 3 semanas

---

## 🔍 VERIFICACIÓN FINAL

### Criterios de Éxito

1. ✅ App Móvil puede actualizar stock sin errores
2. ✅ Cambios en App Móvil se reflejan en WebApp en < 5 segundos
3. ✅ Cambios en WebApp se reflejan en App Móvil en < 5 segundos
4. ✅ Stock inicial se crea automáticamente al crear producto
5. ✅ Precios se muestran correctamente en todas las pantallas
6. ✅ No hay placeholders con `id < 0`
7. ✅ Filtros por empresa funcionan correctamente

---

## 📝 NOTAS IMPORTANTES

### Confirmaciones del Backend

1. ✅ `POST /api/saas/stock` acepta ambos formatos (`camelCase` y `snake_case`)
2. ❌ `PUT /api/saas/stock/{id}` NO existe (nunca existió)
3. ❌ El backend NO crea stock automáticamente al crear producto
4. ✅ Todo se filtra automáticamente por `usuario_comercial_id`
5. ✅ El token JWT incluye `usuario_comercial_id` después del login

### Recomendaciones

1. **Usar `camelCase`** en requests (formato preferido)
2. **Crear stock inicial** desde App Móvil después de crear producto
3. **Implementar refresh automático** después de operaciones
4. **Manejar valores `null`** con fallbacks visuales claros

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

1. **Equipo App Móvil:**
   - [ ] Revisar este plan de acción
   - [ ] Iniciar Fase 1 (Corrección Crítica)
   - [ ] Coordinar con WebApp para pruebas de sincronización

2. **Equipo WebApp:**
   - [ ] Preparar escenarios de prueba
   - [ ] Coordinar con App Móvil para validación cruzada
   - [ ] Documentar cualquier diferencia encontrada

3. **Equipo Backend:**
   - [ ] Confirmar que no hay cambios necesarios
   - [ ] Estar disponible para consultas durante la implementación
   - [ ] Validar que los endpoints funcionan correctamente

---

**Última actualización:** 2025-01-XX  
**Estado:** 🟡 En progreso  
**Próxima revisión:** Después de completar Fase 1
