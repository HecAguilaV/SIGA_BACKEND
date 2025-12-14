# Resumen de Limpieza de Base de Datos

**Fecha:** 2025-01-XX  
**Script ejecutado:** `017_limpiar_todo_excepto_planes.sql`  
**Estado:** ✅ Completado exitosamente

---

## 📊 RESULTADOS DE LA LIMPIEZA

### Datos Eliminados

| Tabla | Registros Antes | Registros Después |
|-------|----------------|-------------------|
| **Productos** | ? | ✅ 0 |
| **Locales** | ? | ✅ 0 |
| **Usuarios Operativos** | ? | ✅ 0 |
| **Usuarios Comerciales** | ? | ✅ 0 |
| **Suscripciones** | ? | ✅ 0 |
| **Facturas** | ? | ✅ 0 |
| **Stock** | ? | ✅ 0 |
| **Ventas** | ? | ✅ 0 |
| **Categorías** | ? | ✅ 0 |
| **Usuarios Permisos** | ? | ✅ 0 |

### Datos Mantenidos

| Tabla | Registros |
|-------|-----------|
| **Planes** | ✅ 2 (mantenidos) |

---

## 🎯 PRÓXIMOS PASOS

### 1. Registrar Nuevo Usuario en Web Comercial

**Endpoint:** `POST /api/comercial/auth/register`

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "password123",
  "nombre": "Juan",
  "apellido": "Pérez",
  "nombreEmpresa": "Mi Empresa"
}
```

**Resultado:** Se crea usuario comercial con `id = 1` (secuencia reseteada)

---

### 2. Crear Suscripción

**Endpoint:** `POST /api/comercial/suscripciones`

**Request:**
```json
{
  "planId": 1,
  "periodo": "MENSUAL"
}
```

**Resultado:**
- ✅ Se crea suscripción activa
- ✅ Se crea usuario operativo automáticamente con:
  - `rol: "ADMINISTRADOR"`
  - `usuario_comercial_id: 1` (asignado automáticamente)
  - Mismo email y contraseña que el usuario comercial

---

### 3. Login en WebApp/App Móvil

**Endpoint:** `POST /api/auth/login`

**Credenciales:** Email y password del usuario comercial

**Resultado:**
- ✅ Auto-asigna empresa si no tiene (ya tiene por creación automática)
- ✅ Retorna `rol: "ADMINISTRADOR"`
- ✅ Retorna `nombreEmpresa: "Mi Empresa"`
- ✅ Retorna `localPorDefecto: null` (aún no hay locales)

---

### 4. Crear Locales, Productos, etc.

**Endpoints:**
- `POST /api/saas/locales` - Crea local con `usuario_comercial_id` asignado automáticamente
- `POST /api/saas/productos` - Crea producto con `usuario_comercial_id` asignado automáticamente
- `POST /api/saas/categorias` - Crea categoría con `usuario_comercial_id` asignado automáticamente
- `POST /api/saas/stock` - Crea stock (producto y local deben tener `usuario_comercial_id`)

**Resultado:** Todo se crea con empresa asignada automáticamente

---

## ✅ VERIFICACIONES REALIZADAS

- ✅ Todos los datos operativos eliminados
- ✅ Todos los datos comerciales eliminados (excepto planes)
- ✅ Secuencias reseteadas (IDs empiezan desde 1)
- ✅ Planes mantenidos (2 planes disponibles)
- ✅ Estructura de tablas intacta

---

## 📝 NOTAS IMPORTANTES

1. **Separación por empresa:** Todos los nuevos datos se crearán con `usuario_comercial_id` asignado automáticamente
2. **Usuario operativo:** Se crea automáticamente al crear suscripción
3. **Rol inicial:** El primer usuario operativo es `ADMINISTRADOR`
4. **Stock inicial:** No se crea automáticamente, debe crearse manualmente o desde App Móvil

---

**Limpieza completada:** ✅  
**Base de datos lista para:** Empezar desde cero con separación por empresa funcionando correctamente
