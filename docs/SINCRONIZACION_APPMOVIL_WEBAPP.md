# Sincronización App Móvil ↔ WebApp

**Fecha:** 2025-01-XX  
**Estado:** ✅ AMBOS USAN LOS MISMOS ENDPOINTS  
**Sincronización:** Automática (tiempo real)

---

## 🎯 PRINCIPIO FUNDAMENTAL

**App Móvil y WebApp usan EXACTAMENTE los mismos endpoints del backend.**

Esto garantiza que:
- ✅ Los cambios en App Móvil se reflejan inmediatamente en WebApp
- ✅ Los cambios en WebApp se reflejan inmediatamente en App Móvil
- ✅ No hay necesidad de sincronización manual
- ✅ Ambos ven los mismos datos en tiempo real

---

## 📋 ENDPOINTS COMPARTIDOS

### Autenticación
- `POST /api/auth/login` - Login operativo
- `POST /api/auth/register` - Registro operativo
- `POST /api/auth/refresh` - Renovar token
- `GET /api/auth/me` - Obtener perfil actual

### Productos
- `GET /api/saas/productos` - Listar productos
- `GET /api/saas/productos/{id}` - Obtener producto
- `POST /api/saas/productos` - Crear producto
- `PUT /api/saas/productos/{id}` - Actualizar producto
- `DELETE /api/saas/productos/{id}` - Eliminar producto

### Stock
- `GET /api/saas/stock` - Listar stock
- `GET /api/saas/stock/{productoId}/{localId}` - Obtener stock específico
- `POST /api/saas/stock` - Crear o actualizar stock

### Locales
- `GET /api/saas/locales` - Listar locales
- `GET /api/saas/locales/{id}` - Obtener local
- `POST /api/saas/locales` - Crear local
- `PUT /api/saas/locales/{id}` - Actualizar local
- `DELETE /api/saas/locales/{id}` - Eliminar local

### Categorías
- `GET /api/saas/categorias` - Listar categorías
- `GET /api/saas/categorias/{id}` - Obtener categoría
- `POST /api/saas/categorias` - Crear categoría
- `PUT /api/saas/categorias/{id}` - Actualizar categoría
- `DELETE /api/saas/categorias/{id}` - Eliminar categoría

### Ventas (en desarrollo)
- `GET /api/saas/ventas` - **Pendiente de publicación (Q1 2026)**
- `POST /api/saas/ventas` - **Pendiente de publicación (Q1 2026)**

### Usuarios
- `GET /api/saas/usuarios` - Listar usuarios
- `POST /api/saas/usuarios` - Crear usuario
- `PUT /api/saas/usuarios/{id}` - Actualizar usuario
- `DELETE /api/saas/usuarios/{id}` - Desactivar usuario

### Chat/Asistente
- `POST /api/saas/chat` - Enviar mensaje al asistente

---

## 🔄 FLUJO DE SINCRONIZACIÓN

### Escenario 1: Agregar Producto en App Móvil
1. Usuario en App Móvil crea producto → `POST /api/saas/productos`
2. Backend guarda en base de datos
3. Usuario en WebApp hace refresh o recarga lista → `GET /api/saas/productos`
4. **Resultado:** Producto aparece en WebApp inmediatamente

### Escenario 2: Editar Stock en WebApp
1. Usuario en WebApp actualiza stock → `POST /api/saas/stock`
2. Backend actualiza en base de datos
3. Usuario en App Móvil hace refresh o recarga lista → `GET /api/saas/stock`
4. **Resultado:** Stock actualizado aparece en App Móvil inmediatamente

### Escenario 3: Crear Venta en App Móvil
1. Usuario en App Móvil crea venta → `POST /api/saas/ventas`
2. Backend guarda venta y actualiza stock automáticamente
3. Usuario en WebApp consulta ventas → `GET /api/saas/ventas`
4. Usuario en WebApp consulta stock → `GET /api/saas/stock`
5. **Resultado:** Venta y stock actualizado aparecen en WebApp inmediatamente

---

## ⚠️ NO HAY DIFERENCIAS

### ❌ NO existen endpoints diferentes para App Móvil
- No hay `/api/mobile/*`
- No hay `/api/app-movil/*`
- No hay lógica especial para móvil

### ❌ NO existen endpoints diferentes para WebApp
- No hay `/api/web/*`
- No hay `/api/webapp/*`
- No hay lógica especial para web

### ✅ Ambos usan exactamente lo mismo
- Misma base: `/api/saas/*`
- Misma autenticación: `/api/auth/*`
- Mismos formatos de request/response
- Misma lógica de negocio

---

## 🔍 VERIFICACIÓN TÉCNICA

### Base URL
Ambos deben usar la misma base URL:
```
https://siga-backend-production.up.railway.app
```

### Autenticación
Ambos usan JWT Bearer Token:
```
Authorization: Bearer <token>
```

### Formatos de Request/Response
Ambos usan JSON:
```json
Content-Type: application/json
```

### Filtrado por Empresa
Ambos filtran automáticamente por `usuario_comercial_id`:
- Solo ven datos de su empresa
- Los cambios se aplican a su empresa
- No hay mezcla de datos entre empresas

---

## 📱 DIFERENCIAS PERMITIDAS (Solo UI/UX)

Las únicas diferencias permitidas son en la **interfaz de usuario**, no en los endpoints:

### App Móvil
- UI optimizada para pantallas pequeñas
- Navegación táctil
- Diseño responsive móvil
- Puede tener funcionalidades específicas de móvil (cámara, GPS, etc.)

### WebApp
- UI optimizada para pantallas grandes
- Navegación con mouse/teclado
- Diseño responsive web
- Puede tener funcionalidades específicas de web (impresión, exportación, etc.)

### ⚠️ IMPORTANTE
**Estas diferencias son solo visuales/de UX. Los datos y operaciones son idénticos.**

---

## 🐛 PROBLEMAS COMUNES Y SOLUCIONES

### Problema 1: "No veo los cambios del otro dispositivo"
**Causa:** No se está haciendo refresh de la lista después de cambios.

**Solución:**
- Implementar refresh automático después de crear/actualizar
- O permitir refresh manual (pull-to-refresh en móvil, botón refresh en web)

### Problema 2: "Los datos están desincronizados"
**Causa:** Cache local o no se está consultando el backend.

**Solución:**
- Siempre consultar el backend para obtener datos actualizados
- No confiar solo en cache local
- Invalidar cache después de operaciones de escritura

### Problema 3: "El stock no se actualiza"
**Causa:** No se está usando el endpoint correcto o no se recarga la lista.

**Solución:**
- Usar `POST /api/saas/stock` (no `PUT /api/saas/stock/{id}` que no existe)
- Recargar lista de stock después de actualizar
- Verificar que producto y local tengan `usuario_comercial_id` asignado

---

## ✅ CHECKLIST PARA DESARROLLADORES

### App Móvil
- [ ] Usa `/api/saas/*` para todas las operaciones
- [ ] Usa `/api/auth/*` para autenticación
- [ ] Implementa refresh después de crear/actualizar
- [ ] Maneja errores de red correctamente
- [ ] Muestra loading states durante operaciones

### WebApp
- [ ] Usa `/api/saas/*` para todas las operaciones
- [ ] Usa `/api/auth/*` para autenticación
- [ ] Implementa refresh después de crear/actualizar
- [ ] Maneja errores de red correctamente
- [ ] Muestra loading states durante operaciones

---

## 📊 EJEMPLO DE SINCRONIZACIÓN

### Flujo Completo: Agregar Stock

**App Móvil:**
```javascript
// 1. Usuario agrega stock
POST /api/saas/stock
{
  "productoId": 1,
  "localId": 1,
  "cantidad": 50,
  "cantidadMinima": 10
}

// 2. Backend responde
{
  "success": true,
  "message": "Stock actualizado exitosamente",
  "stock": { ... }
}

// 3. App Móvil recarga lista
GET /api/saas/stock
// Muestra stock actualizado
```

**WebApp (simultáneamente o después):**
```javascript
// 1. Usuario consulta stock
GET /api/saas/stock

// 2. Backend responde con datos actualizados
{
  "success": true,
  "stock": [
    {
      "id": 1,
      "producto_id": 1,
      "local_id": 1,
      "cantidad": 50,  // ← Actualizado desde App Móvil
      "min_stock": 10
    }
  ]
}

// 3. WebApp muestra stock actualizado
```

**Resultado:** Ambos ven los mismos datos porque consultan la misma fuente (backend/base de datos).

---

## 🎯 CONCLUSIÓN

**App Móvil y WebApp son dos interfaces diferentes para el mismo backend.**

- ✅ Mismos endpoints
- ✅ Misma base de datos
- ✅ Misma lógica de negocio
- ✅ Sincronización automática

**No hay necesidad de sincronización manual. Los cambios se reflejan automáticamente porque ambos consultan la misma fuente de verdad (backend).**

---

**Última actualización:** 2025-01-XX  
**Revisión:** Código fuente completo verificado
