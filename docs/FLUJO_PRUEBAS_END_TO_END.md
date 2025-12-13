# 🧪 Flujo de Pruebas End-to-End - SIGA

## ✅ Sí, la forma correcta es usar el ecosistema completo como cualquier usuario

Las pruebas end-to-end deben simular el flujo completo de un usuario real desde el registro hasta las operaciones diarias.

---

## 📋 Flujo Completo de Prueba End-to-End

### 1️⃣ Registro y Compra (Web Comercial)

**Usuario:** Dueño/Emprendedor

1. **Registrarse en Web Comercial**
   - Endpoint: `POST /api/comercial/auth/register`
   - Datos: email, password, nombre, apellido, nombreEmpresa (opcional)
   - Resultado: Usuario comercial creado + tokens JWT

2. **Ver planes disponibles**
   - Endpoint: `GET /api/comercial/planes`
   - Resultado: Lista de 2 planes (Emprendedor Pro, Crecimiento)

3. **Comprar plan**
   - Endpoint: `POST /api/comercial/suscripciones`
   - Body: `{"planId": 2, "periodo": "MENSUAL"}`
   - Resultado: 
     - Suscripción creada
     - Trial de 14 días activado automáticamente
     - Usuario operativo ADMINISTRADOR creado automáticamente
     - Factura generada

**Verificar:**
- ✅ Usuario comercial existe
- ✅ Suscripción activa
- ✅ Trial activo (14 días)
- ✅ Usuario operativo ADMINISTRADOR existe con mismo email
- ✅ Factura creada

---

### 2️⃣ Acceso a WebApp (SSO o Login Directo)

**Usuario:** Dueño (ADMINISTRADOR)

**Opción A: SSO desde Web Comercial**
1. Usuario está logueado en Web Comercial (tiene token comercial)
2. Intercambiar token: `POST /api/comercial/auth/obtener-token-operativo`
3. Resultado: Token operativo para acceder a WebApp

**Opción B: Login Directo**
1. Endpoint: `POST /api/auth/login`
2. Credenciales: email + password del usuario operativo
3. Resultado: Token operativo

**Verificar:**
- ✅ Token operativo obtenido
- ✅ Token contiene rol: ADMINISTRADOR
- ✅ Puede acceder a endpoints operativos

---

### 3️⃣ Configuración Inicial (WebApp)

**Usuario:** ADMINISTRADOR

1. **Ver locales disponibles**
   - Endpoint: `GET /api/saas/locales`
   - Resultado: Lista de locales (inicialmente vacía o con datos de prueba)

2. **Crear local/bodega**
   - Endpoint: `POST /api/saas/locales`
   - Body: `{"nombre": "Bodega Central", "direccion": "Calle 123", "ciudad": "Santiago"}`
   - Resultado: Local creado

3. **Ver permisos propios**
   - Endpoint: `GET /api/saas/usuarios/{id}/permisos`
   - Resultado: ADMINISTRADOR tiene todos los permisos (26 permisos)

4. **Crear categoría**
   - Endpoint: `POST /api/saas/categorias`
   - Body: `{"nombre": "Bebidas", "descripcion": "Bebidas y refrescos"}`
   - Resultado: Categoría creada

5. **Crear productos**
   - Endpoint: `POST /api/saas/productos`
   - Body: `{"nombre": "Coca Cola", "categoriaId": 1, "precioUnitario": "1500"}`
   - Resultado: Producto creado

6. **Actualizar stock**
   - Endpoint: `PUT /api/saas/stock/{productoId}/{localId}`
   - Body: `{"cantidad": 100, "cantidadMinima": 10}`
   - Resultado: Stock actualizado

**Verificar:**
- ✅ Local creado
- ✅ Categoría creada
- ✅ Productos creados
- ✅ Stock configurado

---

### 4️⃣ Crear Usuarios Empleados (WebApp)

**Usuario:** ADMINISTRADOR

1. **Crear OPERADOR (empleado de inventario)**
   - Endpoint: `POST /api/saas/usuarios`
   - Body: `{"email": "operador@empresa.com", "password": "pass123", "nombre": "Juan", "apellido": "Pérez", "rol": "OPERADOR"}`
   - Resultado: Usuario OPERADOR creado

2. **Verificar permisos del OPERADOR**
   - Endpoint: `GET /api/saas/usuarios/{id}/permisos`
   - Resultado: OPERADOR tiene 9 permisos (PRODUCTOS_VER, PRODUCTOS_CREAR, PRODUCTOS_ACTUALIZAR, STOCK_VER, STOCK_ACTUALIZAR, etc.)

3. **Crear CAJERO**
   - Endpoint: `POST /api/saas/usuarios`
   - Body: `{"email": "cajero@empresa.com", "password": "pass123", "nombre": "María", "apellido": "González", "rol": "CAJERO"}`
   - Resultado: Usuario CAJERO creado

**Verificar:**
- ✅ OPERADOR creado con permisos correctos
- ✅ CAJERO creado con permisos correctos
- ✅ Ambos usuarios pueden hacer login

---

### 5️⃣ Operaciones desde App Móvil (OPERADOR)

**Usuario:** OPERADOR (empleado de inventario)

1. **Login en App Móvil**
   - Endpoint: `POST /api/auth/login`
   - Credenciales: email y password del OPERADOR
   - Resultado: Token operativo

2. **Seleccionar local**
   - Endpoint: `GET /api/saas/locales`
   - Resultado: Lista de locales disponibles
   - Acción: Usuario selecciona un local (guardar en estado de la app)

3. **Ver productos**
   - Endpoint: `GET /api/saas/productos`
   - Resultado: Lista de productos

4. **Agregar nuevo producto** (OPERADOR puede hacerlo)
   - Endpoint: `POST /api/saas/productos`
   - Body: `{"nombre": "Pepsi", "categoriaId": 1, "precioUnitario": "1400"}`
   - Resultado: Producto creado exitosamente

5. **Ver stock del local seleccionado**
   - Endpoint: `GET /api/saas/stock?localId={id}`
   - Resultado: Stock filtrado por local

6. **Actualizar stock**
   - Endpoint: `PUT /api/saas/stock/{productoId}/{localId}`
   - Body: `{"cantidad": 50, "cantidadMinima": 5}`
   - Resultado: Stock actualizado

**Verificar:**
- ✅ OPERADOR puede ver locales
- ✅ OPERADOR puede crear productos
- ✅ OPERADOR puede actualizar stock
- ✅ OPERADOR NO puede eliminar productos (no tiene permiso)

---

### 6️⃣ Operaciones desde App Móvil (CAJERO)

**Usuario:** CAJERO

1. **Login en App Móvil**
   - Endpoint: `POST /api/auth/login`
   - Credenciales: email y password del CAJERO
   - Resultado: Token operativo

2. **Seleccionar local**
   - Endpoint: `GET /api/saas/locales`
   - Resultado: Lista de locales
   - Acción: Seleccionar local

3. **Ver productos**
   - Endpoint: `GET /api/saas/productos`
   - Resultado: Lista de productos

4. **Ver stock**
   - Endpoint: `GET /api/saas/stock?localId={id}`
   - Resultado: Stock del local

5. **Crear venta**
   - Endpoint: `POST /api/saas/ventas` (si existe)
   - Body: Datos de la venta
   - Resultado: Venta registrada

**Verificar:**
- ✅ CAJERO puede ver productos y stock
- ✅ CAJERO puede crear ventas
- ✅ CAJERO NO puede crear productos (no tiene permiso)
- ✅ CAJERO NO puede actualizar stock (no tiene permiso)

---

### 7️⃣ Asistente IA (WebApp o App Móvil)

**Usuario:** Cualquier usuario operativo con suscripción activa

1. **Chat operativo**
   - Endpoint: `POST /api/saas/chat`
   - Body: `{"message": "¿Qué productos tienen stock bajo?"}`
   - Resultado: Respuesta del asistente IA

**Verificar:**
- ✅ Asistente responde correctamente
- ✅ Asistente puede ejecutar operaciones CRUD si tiene permisos
- ✅ Asistente valida permisos antes de ejecutar acciones

---

## ✅ Checklist de Prueba End-to-End Completa

### Flujo Comercial
- [ ] Registro en Web Comercial funciona
- [ ] Compra de plan funciona
- [ ] Trial de 14 días se activa automáticamente
- [ ] Factura se genera correctamente

### Flujo Operativo - ADMINISTRADOR
- [ ] SSO desde Web Comercial funciona
- [ ] Login directo funciona
- [ ] Puede crear locales
- [ ] Puede crear categorías
- [ ] Puede crear productos
- [ ] Puede actualizar stock
- [ ] Puede crear usuarios OPERADOR/CAJERO
- [ ] Puede asignar permisos adicionales
- [ ] Tiene todos los permisos automáticamente

### Flujo Operativo - OPERADOR
- [ ] Login directo funciona
- [ ] Puede ver locales y seleccionar uno
- [ ] Puede ver productos
- [ ] Puede crear productos ✅
- [ ] Puede actualizar productos ✅
- [ ] Puede ver stock
- [ ] Puede actualizar stock ✅
- [ ] NO puede eliminar productos
- [ ] NO puede crear locales

### Flujo Operativo - CAJERO
- [ ] Login directo funciona
- [ ] Puede ver locales y seleccionar uno
- [ ] Puede ver productos
- [ ] Puede ver stock
- [ ] Puede crear ventas
- [ ] NO puede crear productos
- [ ] NO puede actualizar stock

### Asistente IA
- [ ] Chat comercial funciona (público)
- [ ] Chat operativo funciona (requiere auth + suscripción)
- [ ] Asistente valida permisos antes de ejecutar acciones
- [ ] ADMINISTRADOR puede ejecutar todas las acciones por IA
- [ ] OPERADOR puede ejecutar acciones según sus permisos

---

## 🎯 Casos de Prueba Críticos

### Caso 1: Dueño configura su negocio
1. Registro → Compra plan → Accede WebApp → Crea local → Crea productos → Configura stock
2. **Resultado esperado:** Todo funciona, trial activo

### Caso 2: Empleado de inventario trabaja
1. OPERADOR login → Selecciona local → Crea producto nuevo → Actualiza stock
2. **Resultado esperado:** OPERADOR puede crear productos y actualizar stock

### Caso 3: Cajero realiza venta
1. CAJERO login → Selecciona local → Ve productos → Crea venta
2. **Resultado esperado:** CAJERO puede ver productos y crear ventas, pero NO puede crear productos

### Caso 4: Asistente IA ayuda
1. OPERADOR pregunta al asistente → Asistente crea producto → OPERADOR confirma
2. **Resultado esperado:** Asistente ejecuta acción según permisos del usuario

---

## 📝 Notas para Testing

- **Usar datos reales:** Crear usuarios, productos, locales reales (no mocks)
- **Probar permisos:** Verificar que cada rol solo puede hacer lo que tiene permitido
- **Probar selección de local:** Asegurar que funciona en App Móvil y WebApp
- **Probar trial:** Verificar que trial de 14 días funciona correctamente
- **Probar SSO:** Verificar flujo completo desde Web Comercial a WebApp

---

## 🚨 Errores Comunes en Pruebas

1. **No probar el flujo completo:** Solo probar endpoints individuales
   - ❌ Mal: Probar solo `POST /api/saas/productos` con token hardcodeado
   - ✅ Bien: Registro → Compra → Login → Crear producto

2. **No verificar permisos:** Asumir que todos pueden hacer todo
   - ❌ Mal: Probar que CAJERO puede crear productos
   - ✅ Bien: Verificar que CAJERO NO puede crear productos

3. **No probar selección de local:** Asumir que siempre hay un solo local
   - ❌ Mal: Hardcodear localId = 1
   - ✅ Bien: Listar locales → Seleccionar uno → Usar ese localId

4. **No probar trial:** Asumir que siempre hay suscripción activa
   - ❌ Mal: Crear usuario y asumir que tiene acceso
   - ✅ Bien: Registro → Compra plan → Verificar trial activo → Probar acceso

---

**Conclusión:** Sí, las pruebas end-to-end deben usar el ecosistema completo como cualquier usuario real, siguiendo el flujo completo desde el registro hasta las operaciones diarias.
