# Problemas Reportados y Soluciones - Backend

**Fecha:** 2025-01-XX  
**Estado:** 🔴 Problemas identificados y soluciones propuestas

---

## 🔴 PROBLEMA 1: Web Comercial - "No hay plan activo" pero muestra facturas

### Descripción
Al iniciar sesión en Web Comercial, se muestran las facturas pero aparece el mensaje "no hay plan activo".

### Análisis del Backend

**Endpoints involucrados:**
- `GET /api/comercial/facturas` - **NO verifica suscripción activa** (solo autenticación)
- `GET /api/comercial/suscripciones` - Lista suscripciones pero no indica si hay una activa

**Lógica de verificación de suscripción:**
```kotlin
fun hasActiveSubscription(email: String): Boolean {
    // 1. Verifica trial activo (14 días)
    if (usuario.enTrial && fechaFinTrial > ahora) {
        return true
    }
    
    // 2. Verifica suscripción con estado ACTIVA y fechaFin >= hoy
    val suscripciones = suscripcionRepository.findActiveByEmail(
        email,
        EstadoSuscripcion.ACTIVA,
        LocalDate.now()
    )
    return suscripciones.isNotEmpty()
}
```

**Problema identificado:**
- Si la suscripción tiene `fechaFin` en el pasado, `hasActiveSubscription` retorna `false`
- Las facturas se muestran porque no requieren suscripción activa
- El frontend probablemente verifica la suscripción en otro lugar y muestra el mensaje

### Solución Implementada

**1. Mejorar endpoint de suscripciones:**
- Agregar campos `tieneSuscripcionActiva` y `tieneTrialActivo` en la respuesta
- Esto permite al frontend saber el estado real sin hacer llamadas adicionales

**2. Endpoint adicional (opcional):**
- `GET /api/comercial/suscripciones/estado` - Retorna solo el estado de la suscripción

### Código Implementado

```kotlin
@GetMapping
fun listarSuscripciones(): ResponseEntity<Map<String, Any>> {
    // ... código existente ...
    
    // Agregar información de estado
    val tieneSuscripcionActiva = subscriptionService.hasActiveSubscription(email)
    val tieneTrialActivo = subscriptionService.tieneTrialActivo(email)
    
    return ResponseEntity.ok(mapOf(
        "success" to true,
        "suscripciones" to suscripciones,
        "total" to suscripciones.size,
        "tieneSuscripcionActiva" to tieneSuscripcionActiva,  // ← NUEVO
        "tieneTrialActivo" to tieneTrialActivo,              // ← NUEVO
        "enTrial" to usuario.enTrial,                        // ← NUEVO
        "fechaFinTrial" to usuario.fechaFinTrial?.toString()  // ← NUEVO
    ))
}
```

### Acción para Frontend

**Web Comercial debe:**
- Usar `tieneSuscripcionActiva` o `tieneTrialActivo` de la respuesta de `GET /api/comercial/suscripciones`
- O verificar si hay suscripciones con `estado: "ACTIVA"` y `fechaFin >= hoy`
- Mostrar mensaje de "no hay plan activo" solo si realmente no hay suscripción activa

---

## ✅ PROBLEMA 2: WebApp - Reconocer admin/dueño vs operador

### Descripción
WebApp debe reconocer si el usuario es admin/dueño o operador para mostrar diferentes interfaces.

### Estado del Backend

**✅ YA IMPLEMENTADO:** El backend retorna el rol en el login:

```json
{
  "success": true,
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "rol": "ADMINISTRADOR",  // ← AQUÍ: ADMINISTRADOR, OPERADOR o CAJERO
    "nombreEmpresa": "Mi Empresa",
    "localPorDefecto": { ... }
  }
}
```

**Roles disponibles:**
- `ADMINISTRADOR` - Dueño/admin, tiene todos los permisos
- `OPERADOR` - Operador, permisos limitados
- `CAJERO` - Cajero, permisos mínimos

### Acción para Frontend

**WebApp debe:**
- Leer el campo `rol` de la respuesta del login
- Mostrar diferentes interfaces según el rol:
  - `ADMINISTRADOR`: Acceso completo (usuarios, locales, productos, stock, etc.)
  - `OPERADOR`: Acceso limitado (productos, stock, ventas)
  - `CAJERO`: Solo ventas

**Ejemplo:**
```javascript
if (user.rol === "ADMINISTRADOR") {
  // Mostrar panel de administración
} else if (user.rol === "OPERADOR") {
  // Mostrar panel de operaciones
} else {
  // Mostrar panel de caja
}
```

---

## ⚠️ PROBLEMA 3: Precios no se muestran

### Descripción
Los precios de productos no se visualizan en App Móvil ni WebApp.

### Estado del Backend

**✅ CORRECTO:** El backend retorna `precioUnitario` correctamente:

```json
{
  "success": true,
  "productos": [
    {
      "id": 1,
      "nombre": "Producto 1",
      "precioUnitario": "1000.00",  // ← String, puede ser null
      ...
    }
  ]
}
```

**Posibles causas:**
1. Los productos no tienen precio asignado (`precioUnitario: null`)
2. El frontend busca campo `precio` en lugar de `precioUnitario`
3. El frontend no maneja valores `null` correctamente

### Verificación

**Backend retorna:**
- ✅ Campo: `precioUnitario` (String, nullable)
- ✅ Formato: Decimal como String (ej: "1000.00")
- ✅ Null: Si el producto no tiene precio, retorna `null`

### Acción para Frontend

**App Móvil y WebApp deben:**
1. Usar `producto.precioUnitario` (no `producto.precio`)
2. Manejar valores `null` con fallback visual:
   ```javascript
   const precio = producto.precioUnitario 
     ? `$${producto.precioUnitario}` 
     : "Sin precio configurado"
   ```
3. Verificar que los productos creados tengan `precioUnitario` asignado

---

## ⚠️ PROBLEMA 4: Stock muestra "sin stock"

### Descripción
Tanto App Móvil como WebApp muestran "sin stock" aunque debería haber stock.

### Estado del Backend

**✅ CORRECTO:** El backend retorna stock correctamente:

```json
{
  "success": true,
  "stock": [
    {
      "id": 1,
      "producto_id": 1,
      "local_id": 1,
      "cantidad": 10,
      "min_stock": 5
    }
  ],
  "total": 1
}
```

**Posibles causas:**
1. No hay registros de stock en la base de datos (productos sin stock inicial)
2. El filtro por empresa está devolviendo vacío (productos/locales sin `usuario_comercial_id`)
3. El frontend no está parseando correctamente la respuesta

### Verificación

**Backend retorna:**
- ✅ Campo: `stock` (array)
- ✅ Si no hay stock: `stock: []` y `total: 0`
- ✅ Filtrado por empresa automáticamente

### Acción para Frontend

**App Móvil y WebApp deben:**
1. Verificar que `stock` sea un array (puede estar vacío)
2. Mostrar "Sin stock" solo si `stock.length === 0`
3. Verificar que los productos tengan stock inicial creado
4. Si el stock viene vacío, verificar:
   - Que el usuario tenga `usuario_comercial_id` asignado
   - Que los productos y locales tengan `usuario_comercial_id` asignado

---

## 🔧 PROBLEMA 5: Limpiar base de datos para empezar desde cero

### Descripción
Necesidad de limpiar todos los datos (excepto planes) para empezar desde cero con separación por empresa.

### Solución Implementada

**Script creado:** `017_limpiar_todo_excepto_planes.sql`

**Elimina:**
- ✅ Todos los datos operativos (productos, locales, stock, ventas, usuarios operativos)
- ✅ Todos los datos comerciales (usuarios comerciales, suscripciones, facturas, pagos)

**Mantiene:**
- ✅ Planes (siga_comercial.PLANES)
- ✅ Esquemas y estructura de tablas

**Resetea secuencias:**
- ✅ Todos los IDs empiezan desde 1

### Uso

```sql
-- Ejecutar el script
\i src/main/resources/db/migrations/017_limpiar_todo_excepto_planes.sql
```

**Después de ejecutar:**
1. Registrar nuevo usuario en Web Comercial
2. Crear suscripción (esto crea usuario operativo automáticamente)
3. Hacer login en WebApp/App Móvil
4. Crear productos, locales, etc. (todo con empresa asignada automáticamente)

---

## 📋 RESUMEN DE PROBLEMAS Y SOLUCIONES

| Problema | Responsable | Estado | Acción |
|----------|------------|--------|--------|
| **Web Comercial: "No hay plan activo"** | Backend + Frontend | 🔴 En progreso | Backend mejorado, frontend debe usar nuevos campos |
| **WebApp: Reconocer admin vs operador** | Frontend | ✅ Backend listo | Frontend debe leer campo `rol` del login |
| **Precios no se muestran** | Frontend | ✅ Backend correcto | Frontend debe usar `precioUnitario` y manejar `null` |
| **Stock muestra "sin stock"** | Backend + Frontend | ⚠️ Verificar | Verificar que productos tengan stock inicial |
| **Limpiar base de datos** | Backend | ✅ Script creado | Ejecutar script `017_limpiar_todo_excepto_planes.sql` |

---

## 🎯 ACCIONES INMEDIATAS

### Backend (Completado)
- ✅ Mejorado endpoint de suscripciones con información de estado
- ✅ Creado script de limpieza completa
- ✅ Verificado que roles, precios y stock se retornan correctamente

### Frontend (Pendiente)

**Web Comercial:**
- [ ] Usar `tieneSuscripcionActiva` de la respuesta de suscripciones
- [ ] Mostrar mensaje de "no hay plan activo" solo si realmente no hay suscripción activa

**WebApp:**
- [ ] Leer campo `rol` del login y mostrar interfaces según rol
- [ ] Verificar que precios usen `precioUnitario` (no `precio`)
- [ ] Verificar que stock se muestre correctamente

**App Móvil:**
- [ ] Verificar que precios usen `precioUnitario` (no `precio`)
- [ ] Verificar que stock se muestre correctamente
- [ ] Crear stock inicial al crear producto

---

**Última actualización:** 2025-01-XX
