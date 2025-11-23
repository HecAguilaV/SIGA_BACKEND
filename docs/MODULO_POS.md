# 🧾 Módulo POS (Point of Sale) - SIGA

> **Sistema de ventas físicas integrado con gestión de inventario**

---

## 🎯 Objetivo del Módulo POS

Crear un sistema de punto de venta (POS) integrado que:
- ✅ Permita realizar ventas físicas desde cualquier local
- ✅ Descuente automáticamente el inventario al realizar una venta
- ✅ Registre todas las ventas en tiempo real
- ✅ Se integre perfectamente con el módulo de inventario existente
- ✅ Funcione desde la app móvil y web
- ✅ Permita análisis de ventas mediante el asistente

---

## 🏗️ Arquitectura del Módulo POS

### ¿Nuevo Repositorio o Mismo Backend?

**Decisión: Mismo Backend, Mismo Esquema**

El POS es parte integral del sistema operativo, por lo tanto:

✅ **Mismo Backend** (`SIGA_Backend`)
- Agregar módulo POS al backend existente
- Nuevos endpoints: `/api/pos/*`
- Misma autenticación y permisos

✅ **Mismo Esquema** (`siga_saas`)
- Las tablas de ventas ya existen en el diseño
- Solo necesitamos agregar tablas específicas de POS
- Todo relacionado con operaciones del negocio

### Estructura Propuesta

```
Backend (Monolito Modular)
│
├── Módulo: Comercial (ya existe)
│
├── Módulo: SaaS (ya existe)
│   ├── Inventario
│   └── Ventas (básico)
│
├── Módulo: POS (NUEVO) ← Agregar aquí
│   ├── POSRoutes
│   ├── TransaccionesService
│   ├── TurnosCajaService
│   └── DescuentoInventarioService
│
└── Módulo: Asistentes IA (ya existe)
    └── Actualizar para incluir operaciones POS
```

---

## 🗄️ Modelo de Datos para POS

### Tablas en `siga_saas`

#### Tablas Existentes (ya diseñadas)
```sql
-- Ya existen en el diseño
VENTAS (
    id, local_id, usuario_id, fecha, total, estado
)

DETALLES_VENTA (
    id, venta_id, producto_id, cantidad, precio_unitario, subtotal
)
```

#### Tablas Nuevas para POS

```sql
-- Turnos de caja
TURNOS_CAJA (
    id SERIAL PRIMARY KEY,
    local_id INTEGER REFERENCES LOCALES(id),
    usuario_id INTEGER REFERENCES USUARIOS(id),
    fecha_apertura TIMESTAMP,
    fecha_cierre TIMESTAMP,
    monto_inicial DECIMAL(10,2),
    monto_final DECIMAL(10,2),
    estado VARCHAR(20) -- 'ABIERTO', 'CERRADO'
)

-- Transacciones POS
TRANSACCIONES_POS (
    id SERIAL PRIMARY KEY,
    venta_id INTEGER REFERENCES VENTAS(id),
    turno_caja_id INTEGER REFERENCES TURNOS_CAJA(id),
    metodo_pago_id INTEGER REFERENCES METODOS_PAGO(id),
    monto DECIMAL(10,2),
    cambio DECIMAL(10,2), -- Si pagó con efectivo
    fecha TIMESTAMP,
    estado VARCHAR(20) -- 'COMPLETADA', 'CANCELADA', 'REEMBOLSADA'
)

-- Métodos de pago
METODOS_PAGO (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50), -- 'EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'TRANSFERENCIA'
    activo BOOLEAN DEFAULT true
)

-- Items del carrito POS (temporal, en memoria o Redis)
-- O usar tabla temporal si necesitas persistencia
CARRITO_POS (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES USUARIOS(id),
    local_id INTEGER REFERENCES LOCALES(id),
    producto_id INTEGER REFERENCES PRODUCTOS(id),
    cantidad INTEGER,
    precio_unitario DECIMAL(10,2),
    fecha_creacion TIMESTAMP DEFAULT NOW()
)
```

### Relaciones

```
VENTAS (1) ──→ (N) DETALLES_VENTA
VENTAS (1) ──→ (1) TRANSACCIONES_POS
TRANSACCIONES_POS (N) ──→ (1) TURNOS_CAJA
TRANSACCIONES_POS (N) ──→ (1) METODOS_PAGO
TURNOS_CAJA (N) ──→ (1) LOCALES
TURNOS_CAJA (N) ──→ (1) USUARIOS
```

---

## 🔄 Flujo de una Venta POS

### Flujo Completo

```
1. Usuario abre turno de caja
   ↓
2. Usuario escanea/agrega productos al carrito
   ↓
3. Usuario selecciona método de pago
   ↓
4. Sistema calcula total y cambio (si efectivo)
   ↓
5. Usuario confirma venta
   ↓
6. Sistema crea registro en VENTAS
   ↓
7. Sistema crea DETALLES_VENTA (por cada producto)
   ↓
8. Sistema DESCUENTA automáticamente de STOCK
   ↓
9. Sistema crea MOVIMIENTO (tipo: VENTA)
   ↓
10. Sistema crea TRANSACCIONES_POS
    ↓
11. Sistema genera comprobante/ticket
    ↓
12. Venta completada ✅
```

### Descuento Automático de Inventario

**Implementación**:

```kotlin
class POSService {
    suspend fun procesarVenta(ventaRequest: VentaRequest): Venta {
        // 1. Validar stock disponible
        val productosSinStock = validarStock(ventaRequest.items)
        if (productosSinStock.isNotEmpty()) {
            throw InsufficientStockException(productosSinStock)
        }
        
        // 2. Crear venta
        val venta = crearVenta(ventaRequest)
        
        // 3. Crear detalles de venta
        val detalles = crearDetallesVenta(venta.id, ventaRequest.items)
        
        // 4. DESCONTAR STOCK (automático)
        detalles.forEach { detalle ->
            descontarStock(
                productoId = detalle.productoId,
                localId = ventaRequest.localId,
                cantidad = detalle.cantidad
            )
        }
        
        // 5. Crear movimiento
        crearMovimiento(
            tipo = TipoMovimiento.VENTA,
            ventaId = venta.id
        )
        
        // 6. Crear transacción POS
        crearTransaccionPOS(venta.id, ventaRequest.metodoPago)
        
        return venta
    }
    
    private suspend fun descontarStock(
        productoId: Int,
        localId: Int,
        cantidad: Int
    ) {
        // Usar transacción para atomicidad
        transaction {
            val stock = Stock.find {
                Stock.productoId eq productoId and
                Stock.localId eq localId
            }.firstOrNull()
            
            if (stock == null || stock.cantidad < cantidad) {
                throw InsufficientStockException("Stock insuficiente")
            }
            
            Stock.update({ Stock.id eq stock.id }) {
                it.cantidad = stock.cantidad - cantidad
                it.fechaActualizacion = DateTime.now()
            }
        }
    }
}
```

---

## 📡 Endpoints del Módulo POS

### Turnos de Caja

```
POST /api/pos/turnos/abrir
Body: { "local_id": 1, "monto_inicial": 50000 }
Response: { "turno_id": 123, "fecha_apertura": "..." }

GET /api/pos/turnos/{turno_id}
Response: { "turno": {...}, "ventas": [...], "total": 150000 }

POST /api/pos/turnos/{turno_id}/cerrar
Body: { "monto_final": 200000 }
Response: { "turno": {...}, "resumen": {...} }
```

### Carrito y Ventas

```
POST /api/pos/carrito/agregar
Body: { "producto_id": 1, "cantidad": 2, "local_id": 1 }

GET /api/pos/carrito
Response: { "items": [...], "total": 15000 }

POST /api/pos/ventas
Body: {
    "local_id": 1,
    "turno_caja_id": 123,
    "items": [
        { "producto_id": 1, "cantidad": 2, "precio_unitario": 5000 }
    ],
    "metodo_pago_id": 1,
    "monto_recibido": 20000
}
Response: { 
    "venta": {...},
    "cambio": 10000,
    "ticket": "base64_pdf"
}

GET /api/pos/ventas/{venta_id}
Response: { "venta": {...}, "detalles": [...], "transaccion": {...} }
```

### Métodos de Pago

```
GET /api/pos/metodos-pago
Response: [
    { "id": 1, "nombre": "EFECTIVO" },
    { "id": 2, "nombre": "TARJETA_DEBITO" },
    { "id": 3, "nombre": "TARJETA_CREDITO" }
]
```

---

## 🤖 Integración con el Asistente

### Nuevas Capacidades del Asistente

El asistente operativo debe poder:

**Consultas**:
- "¿Cuántas ventas hice hoy?"
- "¿Cuál fue el producto más vendido esta semana?"
- "Muéstrame un gráfico de ventas por local"
- "¿Cuánto facturé este mes?"

**Operaciones**:
- "Abre turno de caja en ITR con $50.000"
- "Cierra el turno de caja actual"
- "Muestra el resumen del turno"

**Análisis**:
- "Dame insights sobre las ventas de esta semana"
- "¿Qué productos debería reponer según las ventas?"
- "Compara ventas entre ITR y Presidente Ibáñez"

### Implementación en RAG

```kotlin
class OperationalAssistantService {
    suspend fun buildRAGContext(userId: String, query: String, role: Role): String {
        val context = mutableListOf<String>()
        
        // ... contexto de inventario existente ...
        
        // Agregar contexto de ventas/POS si es relevante
        if (isSalesQuery(query)) {
            context.add("=== VENTAS Y POS ===")
            context.add(getVentasHoy(userId))
            context.add(getProductosMasVendidos(userId))
            context.add(getResumenTurnoActual(userId))
        }
        
        return context.joinToString("\n")
    }
    
    private fun isSalesQuery(query: String): Boolean {
        val salesKeywords = listOf(
            "venta", "vender", "vendido", "factura", 
            "turno", "caja", "pos", "producto más vendido"
        )
        return salesKeywords.any { query.lowercase().contains(it) }
    }
}
```

---

## 📱 Interfaz del POS

### App Android

**Pantalla Principal POS**:
- Botón "Abrir Turno"
- Lista de productos (con búsqueda)
- Carrito flotante
- Botón "Finalizar Venta"
- Selector de método de pago
- Calculadora de cambio

**Pantalla de Venta**:
- Lista de items en carrito
- Total a pagar
- Input de monto recibido
- Cálculo automático de cambio
- Botón "Confirmar Venta"
- Generación de ticket

### Web (app.siga.com)

Similar a la app móvil, pero optimizado para pantalla grande:
- Vista de productos en grid
- Carrito lateral
- Panel de métodos de pago
- Historial de ventas del día

---

## 🔐 Permisos y Roles

### Nuevo Rol: CAJERO

**Permisos**:
- ✅ Abrir/cerrar turnos de caja
- ✅ Realizar ventas POS
- ✅ Ver ventas de su turno
- ✅ Consultar stock (solo lectura)
- ❌ NO puede modificar productos
- ❌ NO puede ver información del plan
- ❌ NO puede ver reportes completos (solo su turno)

### Matriz de Permisos POS

| Acción | ADMINISTRADOR | OPERADOR | CAJERO |
|--------|---------------|----------|--------|
| Abrir turno | ✅ Sí | ✅ Sí | ✅ Sí |
| Realizar venta | ✅ Sí | ✅ Sí | ✅ Sí |
| Ver todas las ventas | ✅ Sí | ❌ No | ❌ No |
| Ver ventas de su turno | ✅ Sí | ✅ Sí | ✅ Sí |
| Cerrar turno de otro | ✅ Sí | ❌ No | ❌ No |
| Ver reportes completos | ✅ Sí | ❌ No | ❌ No |

---

## 🚀 Plan de Implementación

### Fase 1: Base de Datos
- [ ] Crear tablas: TURNOS_CAJA, TRANSACCIONES_POS, METODOS_PAGO
- [ ] Insertar métodos de pago iniciales
- [ ] Crear relaciones con tablas existentes

### Fase 2: Backend
- [ ] Crear módulo POS en backend
- [ ] Implementar endpoints de turnos
- [ ] Implementar endpoints de ventas
- [ ] Implementar descuento automático de stock
- [ ] Agregar validaciones de stock

### Fase 3: Integración con Asistente
- [ ] Actualizar RAG para incluir contexto de ventas
- [ ] Agregar comandos de ventas al asistente
- [ ] Implementar generación de gráficos

### Fase 4: Frontend
- [ ] Crear pantalla POS en app Android
- [ ] Crear pantalla POS en web
- [ ] Implementar carrito
- [ ] Implementar cálculo de cambio
- [ ] Generar tickets/comprobantes

### Fase 5: Testing
- [ ] Tests de descuento de stock
- [ ] Tests de turnos de caja
- [ ] Tests de integración completa

---

## ❓ Preguntas Frecuentes

### ¿Necesita otro repositorio?
**No**. El POS es parte del sistema operativo, va en el mismo backend.

### ¿Necesita otro schema?
**No**. Todo va en `siga_saas` porque son operaciones del negocio.

### ¿Es difícil agregarlo después?
**No es difícil**. La estructura está preparada:
- Las tablas VENTAS y DETALLES_VENTA ya existen
- Solo agregar tablas específicas de POS
- El descuento de stock es lógica de negocio directa

### ¿Puede funcionar offline?
**Sí, en el futuro**. Se puede implementar:
- Sincronización cuando hay conexión
- Almacenamiento local en la app
- Queue de ventas pendientes

---

## 📊 Ejemplo de Uso Real

### Escenario: Venta en Local ITR

```
1. Cajero abre turno: "Abre turno en ITR con $50.000"
   Asistente: "Turno abierto. Monto inicial: $50.000"

2. Cliente compra:
   - 2x Café Frío ($5.000 c/u)
   - 1x Leche Chocolate ($3.000)
   Total: $13.000

3. Cajero: "Vende 2 café frío y 1 leche chocolate"
   Sistema: Crea venta, descuenta stock automáticamente

4. Cliente paga con $20.000 en efectivo
   Sistema: Calcula cambio ($7.000), genera ticket

5. Al final del día:
   Dueño: "Muéstrame las ventas de hoy en ITR"
   Asistente: "Hoy vendiste $150.000 en 25 transacciones. 
              Producto más vendido: Café Frío (15 unidades)"
```

---

## 🎯 Resumen

- ✅ **Mismo Backend**: Agregar módulo POS al backend existente
- ✅ **Mismo Schema**: Todo en `siga_saas`
- ✅ **Descuento Automático**: Al realizar venta, se descuenta stock
- ✅ **Integración con Asistente**: El asistente puede consultar y operar ventas
- ✅ **Roles**: Agregar rol CAJERO con permisos específicos
- ✅ **No es difícil**: La estructura está preparada para esto

**El POS es una extensión natural del sistema de inventario, no un sistema separado.**

---

**Última actualización**: Diciembre 2024

