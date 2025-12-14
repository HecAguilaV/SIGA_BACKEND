# 🏢 Separación de Datos por Empresa - SIGA

**Fecha:** 2025-01-XX  
**Prioridad:** 🔴 CRÍTICA  
**Estado:** ✅ IMPLEMENTADO

---

## 📋 PROBLEMA IDENTIFICADO

Cuando múltiples usuarios comerciales (dueños) se registran y compran planes, todos sus usuarios operativos aparecían en la misma lista sin separación. Esto significa que:

- ❌ Un dueño podía ver usuarios de otros dueños
- ❌ No había separación de datos entre empresas
- ❌ Todos los usuarios operativos estaban mezclados

---

## ✅ SOLUCIÓN IMPLEMENTADA

### 1. Campo `usuario_comercial_id` en Usuarios Operativos

Se agregó un campo `usuario_comercial_id` a la tabla `siga_saas.USUARIOS` que relaciona cada usuario operativo con su usuario comercial (dueño/empresa).

**Migración SQL:** `013_add_usuario_comercial_id.sql`

```sql
ALTER TABLE siga_saas.USUARIOS 
ADD COLUMN usuario_comercial_id INTEGER REFERENCES siga_comercial.USUARIOS(id) ON DELETE CASCADE;
```

### 2. Asignación Automática al Crear Suscripción

Cuando un usuario comercial compra un plan, el usuario operativo ADMINISTRADOR se crea automáticamente con el `usuario_comercial_id` asignado:

```kotlin
val usuarioOperativo = UsuarioSaas(
    email = usuario.email,
    passwordHash = usuario.passwordHash,
    nombre = usuario.nombre,
    apellido = usuario.apellido,
    rol = Rol.ADMINISTRADOR,
    usuarioComercialId = usuario.id, // ⬅️ Relacionado con empresa
    // ...
)
```

### 3. Filtrado de Usuarios por Empresa

El endpoint `GET /api/saas/usuarios` ahora filtra automáticamente los usuarios por empresa:

- ✅ Solo muestra usuarios de la misma empresa que el usuario actual
- ✅ Si el usuario actual tiene `usuario_comercial_id`, filtra por ese ID
- ✅ Si es usuario legacy (sin `usuario_comercial_id`), busca por email en usuarios comerciales

### 4. Creación de Usuarios con Misma Empresa

Cuando un ADMINISTRADOR crea un nuevo usuario operativo (OPERADOR/CAJERO), automáticamente se asigna la misma empresa:

```kotlin
val nuevoUsuario = UsuarioSaas(
    // ...
    usuarioComercialId = usuarioComercialId, // ⬅️ Misma empresa que el creador
    // ...
)
```

---

## 🔄 COMPORTAMIENTO ACTUAL

### Para Usuarios Nuevos (con suscripción)

1. Usuario comercial se registra → `UsuarioComercial` creado
2. Usuario comercial compra plan → `UsuarioSaas` (ADMINISTRADOR) creado con `usuario_comercial_id`
3. ADMINISTRADOR crea OPERADOR/CAJERO → Nuevo usuario con mismo `usuario_comercial_id`
4. Listado de usuarios → Solo muestra usuarios de la misma empresa

### Para Usuarios Legacy (sin `usuario_comercial_id`)

- El sistema intenta relacionarlos automáticamente por email
- Si encuentra un `UsuarioComercial` con el mismo email, asigna el `usuario_comercial_id`
- Si no encuentra, el usuario queda sin empresa (solo se muestra a sí mismo)

---

## 📝 MIGRACIÓN REQUERIDA

**Ejecutar en la base de datos:**

```sql
-- Ver archivo: src/main/resources/db/migrations/013_add_usuario_comercial_id.sql
```

Esta migración:
1. Agrega el campo `usuario_comercial_id` a `siga_saas.USUARIOS`
2. Crea índice para mejorar rendimiento
3. Actualiza usuarios existentes relacionándolos con sus usuarios comerciales por email

---

## ⚠️ NOTAS IMPORTANTES

### Separación Completa Implementada

**✅ COMPLETAMENTE IMPLEMENTADO:**
- ✅ Usuarios operativos separados por empresa
- ✅ Productos separados por empresa
- ✅ Locales separados por empresa
- ✅ Categorías separadas por empresa
- ✅ Stock separado por empresa (filtrado por producto y local)
- ✅ Ventas separadas por empresa
- ✅ Asistente IA filtra por empresa
- ✅ Creación de datos con empresa asignada automáticamente
- ✅ Validación de empresa en actualización/eliminación

**Cada empresa (dueño) tiene sus propios datos completamente aislados.**

---

## 🧪 PRUEBAS

### Escenario 1: Dos Empresas Diferentes

1. **Empresa A:**
   - Registro: `empresaA@test.com`
   - Compra plan → Usuario operativo ADMINISTRADOR creado con `usuario_comercial_id = 1`
   - Crea OPERADOR → Usuario creado con `usuario_comercial_id = 1`

2. **Empresa B:**
   - Registro: `empresaB@test.com`
   - Compra plan → Usuario operativo ADMINISTRADOR creado con `usuario_comercial_id = 2`
   - Crea OPERADOR → Usuario creado con `usuario_comercial_id = 2`

3. **Resultado:**
   - Empresa A solo ve sus usuarios (2 usuarios)
   - Empresa B solo ve sus usuarios (2 usuarios)
   - No hay mezcla entre empresas

---

## 📚 REFERENCIAS

- **Migración:** `src/main/resources/db/migrations/013_add_usuario_comercial_id.sql`
- **Entidad:** `src/main/kotlin/com/siga/backend/entity/UsuarioSaas.kt`
- **Controlador:** `src/main/kotlin/com/siga/backend/controller/UsuariosController.kt`
- **Repositorio:** `src/main/kotlin/com/siga/backend/repository/UsuarioSaasRepository.kt`

---

**Última actualización:** 2025-01-XX  
**Estado:** ✅ IMPLEMENTADO - REQUIERE MIGRACIÓN SQL
