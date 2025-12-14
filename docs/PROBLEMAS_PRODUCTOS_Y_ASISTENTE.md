# 🐛 Problemas Reportados: Productos y Asistente IA

**Fecha:** 2025-01-XX  
**Estado:** 🔍 EN INVESTIGACIÓN

---

## 📋 PROBLEMAS REPORTADOS

### 1. ❌ Productos no muestran precio
**Síntoma:** Los productos se crean pero no muestran el precio en el frontend.

**Posibles causas:**
- El frontend espera `precio` pero el backend retorna `precioUnitario`
- El precio se está guardando como `null` en la base de datos
- Problema de serialización JSON

**Backend retorna:**
```json
{
  "id": 1,
  "nombre": "Fanta",
  "precioUnitario": "1500",  // ⬅️ Campo correcto
  ...
}
```

**Solución Frontend:** Verificar que el modelo de datos use `precioUnitario` o mapee correctamente.

---

### 2. ❌ Productos no muestran nombres / "Producto s/n"
**Síntoma:** Los productos se agregan al inventario como "Producto s/n" (sin nombre).

**Posibles causas:**
- El nombre no se está enviando en el request
- El nombre está vacío o null
- Problema de validación en el backend

**Backend valida:**
- `@NotBlank` en `ProductoRequest.nombre` - **DEBE rechazar nombres vacíos**

**Solución:**
1. Verificar que el frontend envía `nombre` en el request
2. Verificar logs del backend para ver qué se está recibiendo
3. Si el nombre viene vacío, el backend debería retornar `400 Bad Request`

---

### 3. ❌ Asistente SIGA no funciona
**Síntoma:** 
- Solo muestra en App Móvil
- La Web intenta más tarde (timeout)

**Causas identificadas:**
- Error 503 de Gemini API (Service Unavailable)
- Timeout en las peticiones a Gemini
- Falta de manejo de errores amigable

**Soluciones implementadas:**
- ✅ Manejo mejorado de errores 503
- ✅ Manejo de timeouts
- ✅ Respuestas amigables en lugar de excepciones
- ✅ Logs mejorados para debugging

**Cambios en el código:**
- `GeminiService.kt`: Manejo de 503 y timeouts
- `ChatController.kt`: Retorna respuestas amigables en lugar de lanzar excepciones

---

## 🔍 DEBUGGING REQUERIDO

### Para Productos

1. **Verificar request del frontend:**
```bash
# Ver qué se está enviando
POST /api/saas/productos
{
  "nombre": "Fanta",  // ⬅️ ¿Se envía?
  "precioUnitario": "1500"  // ⬅️ ¿Se envía?
}
```

2. **Verificar respuesta del backend:**
```bash
# Ver qué se está retornando
GET /api/saas/productos
```

3. **Verificar base de datos:**
```sql
SELECT id, nombre, precio_unitario, usuario_comercial_id 
FROM siga_saas.PRODUCTOS 
WHERE activo = true;
```

### Para Asistente IA

1. **Verificar logs del backend:**
   - Buscar errores de Gemini API
   - Verificar si hay timeouts
   - Verificar si la API key está configurada

2. **Verificar respuesta del endpoint:**
```bash
POST /api/saas/chat
{
  "message": "lista los productos"
}
```

---

## ✅ CAMBIOS IMPLEMENTADOS

1. **Manejo de errores del asistente IA:**
   - Retorna `503 Service Unavailable` con mensaje amigable
   - Maneja timeouts correctamente
   - No lanza excepciones que rompan el frontend

2. **Mejoras en GeminiService:**
   - Manejo específico de error 503
   - Manejo de timeouts
   - Logs mejorados

---

## 📝 PRÓXIMOS PASOS

1. **Verificar logs del backend en producción** para ver qué está pasando con los productos
2. **Probar crear producto desde frontend** y ver qué se guarda en la BD
3. **Verificar que el filtrado por empresa** no esté ocultando productos
4. **Probar el asistente IA** después del deploy

---

**Última actualización:** 2025-01-XX  
**Estado:** 🔍 REQUIERE DEBUGGING EN PRODUCCIÓN
