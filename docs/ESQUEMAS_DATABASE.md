# Esquemas de Base de Datos - SIGA

Este documento contiene la estructura completa de los esquemas de base de datos para el sistema SIGA. Está diseñado para ser compartido con los equipos de desarrollo frontend (siga-web.comercial, siga-appweb) y móvil (sigaapp-android).

**Base de Datos**: PostgreSQL  
**Esquemas**: `siga_saas` y `siga_comercial`

---

## Índice

1. [Esquema SIGA_SAAS](#esquema-siga_saas)
   - Tablas del Sistema Operativo
   - Relaciones y Foreign Keys
   - Índices
2. [Esquema SIGA_COMERCIAL](#esquema-siga_comercial)
   - Tablas del Portal Comercial
   - Relaciones y Foreign Keys
   - Índices
3. [Validación de Campos](#validación-de-campos)
4. [Notas de Integración](#notas-de-integración)

---

# ESQUEMA SIGA_SAAS

**Descripción**: Sistema operativo de gestión de inventario y ventas

## Tablas

### 1. USUARIOS

Usuarios operativos del sistema (ADMINISTRADOR, OPERADOR, CAJERO)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email del usuario |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash de la contraseña |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del usuario |
| `apellido` | VARCHAR(100) | NULL | Apellido del usuario |
| `rol` | VARCHAR(20) | NOT NULL | Rol: ADMINISTRADOR, OPERADOR, CAJERO |
| `activo` | BOOLEAN | DEFAULT true | Estado del usuario |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Constraints**:
- `usuarios_rol_check`: rol debe ser 'ADMINISTRADOR', 'OPERADOR' o 'CAJERO'
- `usuarios_email_key`: email único

---

### 2. LOCALES

Bodegas/sucursales donde se almacena inventario

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del local |
| `direccion` | TEXT | NULL | Dirección del local |
| `ciudad` | VARCHAR(100) | NULL | Ciudad del local |
| `activo` | BOOLEAN | DEFAULT true | Estado del local |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

---

### 3. USUARIOS_LOCALES

Relación entre usuarios y locales asignados (OPERADOR solo ve sus locales)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `usuario_id` | INTEGER | PK, FK → usuarios.id | ID del usuario |
| `local_id` | INTEGER | PK, FK → locales.id | ID del local |

**Foreign Keys**:
- `usuarios_locales_usuario_id_fkey`: ON DELETE CASCADE
- `usuarios_locales_local_id_fkey`: ON DELETE CASCADE

---

### 4. CATEGORIAS

Categorías de productos

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL, UNIQUE | Nombre de la categoría |
| `descripcion` | TEXT | NULL | Descripción de la categoría |
| `activa` | BOOLEAN | DEFAULT true | Estado de la categoría |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Constraints**:
- `categorias_nombre_key`: nombre único

---

### 5. PRODUCTOS

Catálogo de productos

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(200) | NOT NULL | Nombre del producto |
| `descripcion` | TEXT | NULL | Descripción del producto |
| `categoria_id` | INTEGER | FK → categorias.id | ID de la categoría |
| `codigo_barras` | VARCHAR(50) | UNIQUE | Código de barras |
| `precio_unitario` | NUMERIC(10,2) | NULL | Precio unitario |
| `activo` | BOOLEAN | DEFAULT true | Estado del producto |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Foreign Keys**:
- `productos_categoria_id_fkey`: referencia a categorias.id

**Constraints**:
- `productos_codigo_barras_key`: código de barras único

---

### 6. STOCK

Stock por producto y local

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `producto_id` | INTEGER | NOT NULL, FK → productos.id | ID del producto |
| `local_id` | INTEGER | NOT NULL, FK → locales.id | ID del local |
| `cantidad` | INTEGER | NOT NULL, DEFAULT 0, >= 0 | Cantidad en stock |
| `cantidad_minima` | INTEGER | NOT NULL, DEFAULT 0, >= 0 | Cantidad mínima de alerta |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Foreign Keys**:
- `stock_producto_id_fkey`: ON DELETE CASCADE
- `stock_local_id_fkey`: ON DELETE CASCADE

**Constraints**:
- `stock_producto_id_local_id_key`: UNIQUE (producto_id, local_id)
- `stock_cantidad_check`: cantidad >= 0
- `stock_cantidad_minima_check`: cantidad_minima >= 0

**Índices**:
- `idx_stock_producto`: producto_id
- `idx_stock_local`: local_id

---

### 7. MOVIMIENTOS

Historial de movimientos de stock

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `producto_id` | INTEGER | FK → productos.id | ID del producto |
| `local_id` | INTEGER | FK → locales.id | ID del local |
| `tipo` | VARCHAR(20) | NOT NULL | Tipo: ENTRADA, SALIDA, VENTA, AJUSTE, TRASLADO |
| `cantidad` | INTEGER | NOT NULL | Cantidad del movimiento |
| `cantidad_anterior` | INTEGER | NULL | Cantidad antes del movimiento |
| `cantidad_nueva` | INTEGER | NULL | Cantidad después del movimiento |
| `usuario_id` | INTEGER | FK → usuarios.id | ID del usuario que realizó el movimiento |
| `venta_id` | INTEGER | FK → ventas.id | ID de la venta (si aplica) |
| `observaciones` | TEXT | NULL | Observaciones del movimiento |
| `fecha` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha del movimiento |

**Foreign Keys**:
- `movimientos_producto_id_fkey`: ON DELETE CASCADE
- `movimientos_local_id_fkey`: ON DELETE CASCADE
- `movimientos_usuario_id_fkey`: referencia a usuarios.id
- `fk_movimientos_venta`: ON DELETE SET NULL

**Constraints**:
- `movimientos_tipo_check`: tipo debe ser 'ENTRADA', 'SALIDA', 'VENTA', 'AJUSTE' o 'TRASLADO'

**Índices**:
- `idx_movimientos_producto`: producto_id
- `idx_movimientos_local`: local_id
- `idx_movimientos_fecha`: fecha

---

### 8. VENTAS

Registro de ventas (desde POS y manuales)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `local_id` | INTEGER | FK → locales.id | ID del local |
| `usuario_id` | INTEGER | FK → usuarios.id | ID del usuario que realizó la venta |
| `fecha` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de la venta |
| `total` | NUMERIC(10,2) | NOT NULL, >= 0 | Total de la venta |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'COMPLETADA' | Estado: COMPLETADA, CANCELADA, PENDIENTE |
| `observaciones` | TEXT | NULL | Observaciones de la venta |

**Foreign Keys**:
- `ventas_local_id_fkey`: ON DELETE CASCADE
- `ventas_usuario_id_fkey`: referencia a usuarios.id

**Constraints**:
- `ventas_estado_check`: estado debe ser 'COMPLETADA', 'CANCELADA' o 'PENDIENTE'
- `ventas_total_check`: total >= 0

**Índices**:
- `idx_ventas_local`: local_id
- `idx_ventas_usuario`: usuario_id
- `idx_ventas_fecha`: fecha

---

### 9. DETALLES_VENTA

Detalles de cada venta (productos vendidos)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `venta_id` | INTEGER | NOT NULL, FK → ventas.id | ID de la venta |
| `producto_id` | INTEGER | NOT NULL, FK → productos.id | ID del producto |
| `cantidad` | INTEGER | NOT NULL, > 0 | Cantidad vendida |
| `precio_unitario` | NUMERIC(10,2) | NOT NULL, >= 0 | Precio unitario al momento de la venta |
| `subtotal` | NUMERIC(10,2) | NOT NULL, >= 0 | Subtotal (cantidad * precio_unitario) |

**Foreign Keys**:
- `detalles_venta_venta_id_fkey`: ON DELETE CASCADE
- `detalles_venta_producto_id_fkey`: ON DELETE CASCADE

**Constraints**:
- `detalles_venta_cantidad_check`: cantidad > 0
- `detalles_venta_precio_unitario_check`: precio_unitario >= 0
- `detalles_venta_subtotal_check`: subtotal >= 0

**Índices**:
- `idx_detalles_venta`: venta_id
- `idx_detalles_producto`: producto_id

---

### 10. TURNOS_CAJA

Turnos de caja por local y usuario

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `local_id` | INTEGER | FK → locales.id | ID del local |
| `usuario_id` | INTEGER | FK → usuarios.id | ID del usuario |
| `fecha_apertura` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de apertura |
| `fecha_cierre` | TIMESTAMP | NULL | Fecha de cierre |
| `monto_inicial` | NUMERIC(10,2) | NOT NULL, DEFAULT 0, >= 0 | Monto inicial en caja |
| `monto_final` | NUMERIC(10,2) | NULL, >= 0 | Monto final en caja |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'ABIERTO' | Estado: ABIERTO, CERRADO |

**Foreign Keys**:
- `turnos_caja_local_id_fkey`: ON DELETE CASCADE
- `turnos_caja_usuario_id_fkey`: referencia a usuarios.id

**Constraints**:
- `turnos_caja_estado_check`: estado debe ser 'ABIERTO' o 'CERRADO'
- `turnos_caja_monto_inicial_check`: monto_inicial >= 0
- `turnos_caja_monto_final_check`: monto_final >= 0

**Índices**:
- `idx_turnos_local`: local_id
- `idx_turnos_usuario`: usuario_id
- `idx_turnos_estado`: estado

---

### 11. TRANSACCIONES_POS

Transacciones del punto de venta

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `venta_id` | INTEGER | FK → ventas.id | ID de la venta |
| `turno_caja_id` | INTEGER | FK → turnos_caja.id | ID del turno de caja |
| `metodo_pago_id` | INTEGER | FK → metodos_pago.id | ID del método de pago |
| `monto` | NUMERIC(10,2) | NOT NULL, >= 0 | Monto de la transacción |
| `cambio` | NUMERIC(10,2) | DEFAULT 0, >= 0 | Cambio entregado |
| `fecha` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de la transacción |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'COMPLETADA' | Estado: COMPLETADA, CANCELADA, REEMBOLSADA |

**Foreign Keys**:
- `transacciones_pos_venta_id_fkey`: ON DELETE CASCADE
- `transacciones_pos_turno_caja_id_fkey`: ON DELETE CASCADE
- `transacciones_pos_metodo_pago_id_fkey`: referencia a metodos_pago.id

**Constraints**:
- `transacciones_pos_estado_check`: estado debe ser 'COMPLETADA', 'CANCELADA' o 'REEMBOLSADA'
- `transacciones_pos_monto_check`: monto >= 0
- `transacciones_pos_cambio_check`: cambio >= 0

**Índices**:
- `idx_transacciones_venta`: venta_id
- `idx_transacciones_turno`: turno_caja_id

---

### 12. METODOS_PAGO

Métodos de pago disponibles (EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(50) | NOT NULL, UNIQUE | Nombre del método de pago |
| `activo` | BOOLEAN | DEFAULT true | Estado del método |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Constraints**:
- `metodos_pago_nombre_key`: nombre único

---

### 13. CARRITO_POS

Carrito temporal del POS (puede limpiarse periódicamente)

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `usuario_id` | INTEGER | FK → usuarios.id | ID del usuario |
| `local_id` | INTEGER | FK → locales.id | ID del local |
| `producto_id` | INTEGER | FK → productos.id | ID del producto |
| `cantidad` | INTEGER | NOT NULL, > 0 | Cantidad en el carrito |
| `precio_unitario` | NUMERIC(10,2) | NOT NULL, >= 0 | Precio unitario |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Foreign Keys**:
- `carrito_pos_usuario_id_fkey`: ON DELETE CASCADE
- `carrito_pos_local_id_fkey`: ON DELETE CASCADE
- `carrito_pos_producto_id_fkey`: ON DELETE CASCADE

**Constraints**:
- `carrito_pos_cantidad_check`: cantidad > 0
- `carrito_pos_precio_unitario_check`: precio_unitario >= 0

**Índices**:
- `idx_carrito_usuario`: usuario_id
- `idx_carrito_local`: local_id

---

### 14. ALERTAS

Alertas y notificaciones del sistema

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `tipo` | VARCHAR(50) | NOT NULL | Tipo: STOCK_BAJO, STOCK_AGOTADO, VENTA_ALTA, MOVIMIENTO_SOSPECHOSO |
| `producto_id` | INTEGER | FK → productos.id | ID del producto (si aplica) |
| `local_id` | INTEGER | FK → locales.id | ID del local (si aplica) |
| `mensaje` | TEXT | NOT NULL | Mensaje de la alerta |
| `leida` | BOOLEAN | DEFAULT false | Si la alerta fue leída |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Foreign Keys**:
- `alertas_producto_id_fkey`: ON DELETE CASCADE
- `alertas_local_id_fkey`: ON DELETE CASCADE

**Constraints**:
- `alertas_tipo_check`: tipo debe ser 'STOCK_BAJO', 'STOCK_AGOTADO', 'VENTA_ALTA' o 'MOVIMIENTO_SOSPECHOSO'

**Índices**:
- `idx_alertas_producto`: producto_id
- `idx_alertas_local`: local_id
- `idx_alertas_leida`: leida

---

# ESQUEMA SIGA_COMERCIAL

**Descripción**: Portal comercial y gestión de suscripciones

## Tablas

### 1. USUARIOS

Clientes del portal comercial

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email del cliente |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash de la contraseña |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del cliente |
| `apellido` | VARCHAR(100) | NULL | Apellido del cliente |
| `rut` | VARCHAR(20) | NULL | RUT del cliente |
| `telefono` | VARCHAR(20) | NULL | Teléfono del cliente |
| `activo` | BOOLEAN | DEFAULT true | Estado del cliente |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Constraints**:
- `usuarios_email_key`: email único

---

### 2. PLANES

Planes de suscripción disponibles

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL, UNIQUE | Nombre del plan |
| `descripcion` | TEXT | NULL | Descripción del plan |
| `precio_mensual` | NUMERIC(10,2) | NOT NULL, >= 0 | Precio mensual |
| `precio_anual` | NUMERIC(10,2) | NULL, >= 0 | Precio anual |
| `limite_bodegas` | INTEGER | NOT NULL, DEFAULT 1, > 0 | Límite de bodegas |
| `limite_usuarios` | INTEGER | NOT NULL, DEFAULT 1, > 0 | Límite de usuarios |
| `limite_productos` | INTEGER | NULL | Límite de productos (NULL = ilimitado) |
| `caracteristicas` | JSONB | NULL | JSON con características: {"trial_gratis": true, "soporte": "email", etc} |
| `activo` | BOOLEAN | DEFAULT true | Estado del plan |
| `orden` | INTEGER | DEFAULT 0 | Orden de visualización |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Constraints**:
- `planes_nombre_key`: nombre único
- `planes_precio_mensual_check`: precio_mensual >= 0
- `planes_precio_anual_check`: precio_anual >= 0
- `planes_limite_bodegas_check`: limite_bodegas > 0
- `planes_limite_usuarios_check`: limite_usuarios > 0

---

### 3. SUSCRIPCIONES

Suscripciones activas de clientes

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `usuario_id` | INTEGER | NOT NULL, FK → usuarios.id | ID del cliente |
| `plan_id` | INTEGER | NOT NULL, FK → planes.id | ID del plan |
| `fecha_inicio` | DATE | NOT NULL | Fecha de inicio |
| `fecha_fin` | DATE | NULL | Fecha de fin |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVA' | Estado: ACTIVA, SUSPENDIDA, CANCELADA, VENCIDA |
| `periodo` | VARCHAR(20) | NOT NULL, DEFAULT 'MENSUAL' | Periodo: MENSUAL, ANUAL |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Foreign Keys**:
- `suscripciones_usuario_id_fkey`: ON DELETE CASCADE
- `suscripciones_plan_id_fkey`: referencia a planes.id

**Constraints**:
- `suscripciones_estado_check`: estado debe ser 'ACTIVA', 'SUSPENDIDA', 'CANCELADA' o 'VENCIDA'
- `suscripciones_periodo_check`: periodo debe ser 'MENSUAL' o 'ANUAL'

**Índices**:
- `idx_suscripciones_usuario`: usuario_id
- `idx_suscripciones_plan`: plan_id
- `idx_suscripciones_estado`: estado

---

### 4. PAGOS

Registro de pagos de suscripciones

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `suscripcion_id` | INTEGER | FK → suscripciones.id | ID de la suscripción |
| `monto` | NUMERIC(10,2) | NOT NULL, >= 0 | Monto del pago |
| `moneda` | VARCHAR(10) | DEFAULT 'CLP' | Moneda del pago |
| `metodo_pago` | VARCHAR(50) | NULL | Método de pago |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDIENTE' | Estado: PENDIENTE, COMPLETADO, FALLIDO, REEMBOLSADO |
| `referencia_externa` | VARCHAR(255) | NULL | Referencia externa (ID de transacción) |
| `fecha_pago` | TIMESTAMP | NULL | Fecha del pago |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Foreign Keys**:
- `pagos_suscripcion_id_fkey`: ON DELETE CASCADE

**Constraints**:
- `pagos_estado_check`: estado debe ser 'PENDIENTE', 'COMPLETADO', 'FALLIDO' o 'REEMBOLSADO'
- `pagos_monto_check`: monto >= 0

**Índices**:
- `idx_pagos_suscripcion`: suscripcion_id
- `idx_pagos_estado`: estado
- `idx_pagos_fecha`: fecha_pago

---

### 5. FACTURAS

Facturas generadas para suscripciones

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `suscripcion_id` | INTEGER | FK → suscripciones.id | ID de la suscripción |
| `pago_id` | INTEGER | FK → pagos.id | ID del pago |
| `numero_factura` | VARCHAR(50) | UNIQUE | Número de factura |
| `monto` | NUMERIC(10,2) | NOT NULL, >= 0 | Monto base |
| `iva` | NUMERIC(10,2) | DEFAULT 0, >= 0 | IVA |
| `total` | NUMERIC(10,2) | NOT NULL, >= 0 | Total (monto + iva) |
| `fecha_emision` | DATE | NOT NULL | Fecha de emisión |
| `fecha_vencimiento` | DATE | NULL | Fecha de vencimiento |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDIENTE' | Estado: PENDIENTE, PAGADA, VENCIDA, ANULADA |
| `archivo_pdf` | TEXT | NULL | Ruta/URL del PDF |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |

**Foreign Keys**:
- `facturas_suscripcion_id_fkey`: ON DELETE CASCADE
- `facturas_pago_id_fkey`: referencia a pagos.id

**Constraints**:
- `facturas_numero_factura_key`: numero_factura único
- `facturas_estado_check`: estado debe ser 'PENDIENTE', 'PAGADA', 'VENCIDA' o 'ANULADA'
- `facturas_monto_check`: monto >= 0
- `facturas_iva_check`: iva >= 0
- `facturas_total_check`: total >= 0

**Índices**:
- `idx_facturas_suscripcion`: suscripcion_id
- `idx_facturas_pago`: pago_id
- `idx_facturas_estado`: estado

---

### 6. CARRITOS

Carritos de compra de planes

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | INTEGER | PK, AUTO_INCREMENT | Identificador único |
| `usuario_id` | INTEGER | FK → usuarios.id | ID del cliente |
| `plan_id` | INTEGER | FK → planes.id | ID del plan |
| `periodo` | VARCHAR(20) | NOT NULL, DEFAULT 'MENSUAL' | Periodo: MENSUAL, ANUAL |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de creación |
| `fecha_actualizacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Fecha de última actualización |

**Foreign Keys**:
- `carritos_usuario_id_fkey`: ON DELETE CASCADE
- `carritos_plan_id_fkey`: referencia a planes.id

**Constraints**:
- `carritos_usuario_id_key`: usuario_id único (un carrito por usuario)
- `carritos_periodo_check`: periodo debe ser 'MENSUAL' o 'ANUAL'

**Índices**:
- `idx_carritos_usuario`: usuario_id

---

# VALIDACIÓN DE CAMPOS

## Tipos de Datos Estándar

- **INTEGER**: Números enteros
- **VARCHAR(n)**: Cadenas de texto con longitud máxima
- **TEXT**: Cadenas de texto sin límite de longitud
- **NUMERIC(10,2)**: Números decimales con 10 dígitos totales y 2 decimales
- **BOOLEAN**: true/false
- **TIMESTAMP**: Fecha y hora (sin zona horaria)
- **DATE**: Solo fecha (sin hora)
- **JSONB**: Objeto JSON (PostgreSQL)

## Enums y Valores Permitidos

### SIGA_SAAS

- **usuarios.rol**: `ADMINISTRADOR`, `OPERADOR`, `CAJERO`
- **movimientos.tipo**: `ENTRADA`, `SALIDA`, `VENTA`, `AJUSTE`, `TRASLADO`
- **ventas.estado**: `COMPLETADA`, `CANCELADA`, `PENDIENTE`
- **turnos_caja.estado**: `ABIERTO`, `CERRADO`
- **transacciones_pos.estado**: `COMPLETADA`, `CANCELADA`, `REEMBOLSADA`
- **alertas.tipo**: `STOCK_BAJO`, `STOCK_AGOTADO`, `VENTA_ALTA`, `MOVIMIENTO_SOSPECHOSO`

### SIGA_COMERCIAL

- **suscripciones.estado**: `ACTIVA`, `SUSPENDIDA`, `CANCELADA`, `VENCIDA`
- **suscripciones.periodo**: `MENSUAL`, `ANUAL`
- **pagos.estado**: `PENDIENTE`, `COMPLETADO`, `FALLIDO`, `REEMBOLSADO`
- **facturas.estado**: `PENDIENTE`, `PAGADA`, `VENCIDA`, `ANULADA`
- **carritos.periodo**: `MENSUAL`, `ANUAL`

## Campos Obligatorios vs Opcionales

### Campos NOT NULL (Obligatorios)
- Todos los campos `id` (PK)
- Campos con restricción `NOT NULL` en la definición
- Campos con `DEFAULT` pueden ser omitidos pero se asignará el valor por defecto

### Campos NULL (Opcionales)
- Campos que no tienen restricción `NOT NULL`
- Se pueden enviar como `null` o omitirse en las peticiones

---

# NOTAS DE INTEGRACIÓN

## Relaciones entre Esquemas

Los esquemas `siga_saas` y `siga_comercial` son **independientes** a nivel de base de datos. La relación se establece a nivel de aplicación:

- Un `usuario` en `siga_comercial` puede tener una `suscripcion` activa
- Si la suscripción está activa, el usuario puede acceder al sistema `siga_saas`
- Los usuarios de `siga_saas` son diferentes a los de `siga_comercial` (tablas separadas)

## Consideraciones para Frontend

1. **Autenticación Dual**:
   - Portal comercial: usa `siga_comercial.usuarios`
   - Sistema operativo: usa `siga_saas.usuarios`
   - Pueden ser usuarios diferentes o el mismo email con diferentes contraseñas

2. **Validación de Suscripción**:
   - Antes de acceder a endpoints de `siga_saas`, verificar que el usuario tenga una suscripción activa
   - Validar límites del plan (bodegas, usuarios, productos)

3. **Manejo de Fechas**:
   - `TIMESTAMP`: Enviar como ISO 8601: `"2025-01-15T10:30:00"`
   - `DATE`: Enviar como ISO 8601: `"2025-01-15"`

4. **Manejo de Decimales**:
   - `NUMERIC(10,2)`: Enviar como número o string: `"100.50"` o `100.50`
   - El backend manejará la conversión

5. **Campos JSONB**:
   - `planes.caracteristicas`: Enviar como objeto JSON: `{"trial_gratis": true, "soporte": "email"}`

## Ejemplos de Uso

### Crear Producto (SIGA_SAAS)
```json
{
  "nombre": "Producto Ejemplo",
  "descripcion": "Descripción del producto",
  "categoria_id": 1,
  "codigo_barras": "1234567890123",
  "precio_unitario": 9990.50,
  "activo": true
}
```

### Crear Suscripción (SIGA_COMERCIAL)
```json
{
  "usuario_id": 1,
  "plan_id": 2,
  "fecha_inicio": "2025-01-15",
  "periodo": "MENSUAL"
}
```

### Crear Venta (SIGA_SAAS)
```json
{
  "local_id": 1,
  "usuario_id": 1,
  "total": 19980.00,
  "estado": "COMPLETADA",
  "detalles": [
    {
      "producto_id": 1,
      "cantidad": 2,
      "precio_unitario": 9990.00,
      "subtotal": 19980.00
    }
  ]
}
```

---

**Última actualización**: Diciembre 2025
**Versión del esquema**: 1.0.0 MVP
