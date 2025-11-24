# 🎯 Instrucciones Completas: Desarrollo del Backend SIGA

> **Para el Agente AI que desarrollará el backend**  
> Este documento contiene TODO el contexto necesario para comenzar desde cero.

---

## 📋 Contexto del Proyecto

### ¿Qué es SIGA?

SIGA (Sistema Inteligente de Gestión de Activos) es un **mini ERP para PYMES chilenas** con simplicidad radical y asistente conversacional inteligente.

**Visión**: Un ERP simple donde el asistente puede hacer CRUD completo mediante lenguaje natural, permitiendo operar desde cualquier lugar (mientras cargas tu furgón, le dices al asistente qué hacer). Si un producto no existe, el asistente lo crea automáticamente. El asistente genera gráficos y análisis con insights claros, sin tecnicismos.

**Lema**: **"No gestiones tu Inventario, Gestiona tu Tiempo"**

**Roadmap**:
- ✅ Fase 1: Gestión de Inventario (actual)
- 🔄 Fase 2: Módulo POS (próximo)
- 📋 Fase 3: Contabilidad, RRHH, etc. (futuro)

### Arquitectura del Ecosistema

SIGA tiene **4 componentes principales**:

1. **Web Comercial** (siga.com) - React - Vercel
   - Portal de marketing y venta de suscripciones
   - Público puede ver planes y precios
   - Clientes pueden comprar planes

2. **Prototipo Web** (app.siga.com) - SvelteKit - Vercel
   - Aplicación SaaS operativa
   - Gestión de inventario, stock, ventas
   - Usuarios autenticados con suscripción activa

3. **App Android** - Kotlin + Jetpack Compose
   - Versión móvil nativa del SaaS
   - Mismas funcionalidades que el prototipo web

4. **Backend** (este proyecto) - Kotlin + Ktor
   - API REST única que alimenta a los 3 componentes anteriores
   - Base de datos PostgreSQL con dos esquemas
   - Dos asistentes IA especializados

---

## 🗄️ Arquitectura de Base de Datos

### Una Base de Datos, Dos Esquemas

**PostgreSQL en Always Data** con separación lógica:

#### Esquema: `siga_saas`
**Propósito**: Sistema operativo de gestión de inventario

**Tablas principales**:
- `USUARIOS` - Usuarios operativos (ADMINISTRADOR, OPERADOR)
- `PRODUCTOS` - Catálogo de productos
- `CATEGORIAS` - Categorías de productos
- `LOCALES` - Bodegas/sucursales
- `STOCK` - Stock por producto y local
- `VENTAS` - Registro de ventas
- `DETALLES_VENTA` - Detalles de cada venta
- `MOVIMIENTOS` - Historial de movimientos de stock
- `ALERTAS` - Alertas y notificaciones

**Consumido por**: Prototipo Web + App Android

#### Esquema: `siga_comercial`
**Propósito**: Portal comercial y gestión de suscripciones

**Tablas principales**:
- `USUARIOS` - Clientes del portal
- `PLANES` - Planes de suscripción disponibles
- `SUSCRIPCIONES` - Suscripciones activas de clientes
- `PAGOS` - Registro de pagos
- `FACTURAS` - Facturas generadas
- `CARRITOS` - Carritos de compra

**Consumido por**: Web Comercial

### Relación entre Esquemas

```
Cliente se registra en siga.com
    ↓
Crea cuenta en siga_comercial.USUARIOS
    ↓
Compra plan → siga_comercial.SUSCRIPCIONES
    ↓
Obtiene acceso a app.siga.com
    ↓
Se crea/vincula en siga_saas.USUARIOS
    ↓
Usa el sistema → Datos en siga_saas.*
```

---

## 🤖 Arquitectura de Asistentes IA

### Dos Asistentes Especializados

#### 1. Asistente Comercial
**Endpoint**: `POST /api/comercial/chat`

**Propósito**: Ventas, marketing, soporte pre-venta

**Acceso**:
- ✅ `siga_comercial.*` (planes, precios, características)
- ❌ `siga_saas.*` (SIN acceso a inventario)

**Usuarios**: Público, clientes (con/sin suscripción)

**Ejemplos de respuestas**:
- "¿Qué incluye el plan Emprendedor Pro?"
- "¿Cuánto cuesta el plan Crecimiento?"
- "¿Hay trial gratuito?"

**Si pregunta sobre inventario**: Redirige a app.siga.com

#### 2. Asistente Operativo
**Endpoint**: `POST /api/saas/chat`

**Propósito**: Operaciones del negocio

**Acceso**:
- ✅ `siga_saas.*` (inventario completo según rol)
- ✅ `siga_comercial.SUSCRIPCIONES` (solo su suscripción, según rol)

**Requisitos**: Autenticación JWT + Suscripción activa

**Permisos por Rol**:

**ADMINISTRADOR**:
- Acceso completo a inventario (todos los locales)
- Puede consultar plan, suscripción, precios, límites
- Ejemplo: "¿Qué plan tengo?" → Responde con detalles completos

**OPERADOR**:
- Acceso limitado a inventario (solo locales asignados)
- NO puede ver planes, precios, costos
- Ejemplo: "¿Qué plan tiene la empresa?" → "No tienes permisos. Contacta al administrador"

### Implementación RAG (Retrieval-Augmented Generation)

El asistente usa RAG para:
1. Recuperar datos relevantes de PostgreSQL según la pregunta
2. Construir contexto con esa información
3. Enviar a Gemini con el contexto
4. Gemini genera respuesta basada en datos reales

**Ejemplo de flujo RAG**:
```
Usuario: "¿Cuánto stock hay de Café Frío en ITR?"
    ↓
Backend identifica: pregunta de inventario, usuario OPERADOR
    ↓
RAG consulta: SELECT * FROM siga_saas.STOCK 
              WHERE producto_id = X AND local_id = Y 
              AND local_id IN (locales_asignados_al_usuario)
    ↓
Construye contexto: "Café Frío Listo 350ml: 26 unidades en ITR"
    ↓
Envía a Gemini con contexto SIGA
    ↓
Gemini responde: "Hay 26 unidades de Café Frío Listo 350ml en ITR"
```

---

## 🏗️ Arquitectura del Backend

### Decisión: Monolito Modular

**NO microservicios** (por ahora). Estructura modular dentro de un solo proyecto:

```
Backend (Monolito Modular)
│
├── Módulo: Comercial
│   ├── ChatRoutes (asistente comercial)
│   ├── PlanesRoutes
│   └── SuscripcionesRoutes
│
├── Módulo: SaaS
│   ├── ChatRoutes (asistente operativo)
│   ├── ProductosRoutes
│   ├── StockRoutes
│   └── VentasRoutes (básico)
│
├── Módulo: POS (Point of Sale) ← IMPORTANTE
│   ├── POSRoutes (turnos, ventas, carrito)
│   ├── TransaccionesService
│   ├── TurnosCajaService
│   └── DescuentoInventarioService (descuenta stock automáticamente)
│
├── Módulo: Autenticación
│   ├── AuthRoutes
│   └── JWTService
│
└── Módulo: Asistentes IA
    ├── CommercialAssistantService
    ├── OperationalAssistantService (incluye operaciones POS)
    └── GeminiService
```

### Stack Tecnológico

- **Lenguaje**: Kotlin
- **Framework**: Ktor 2.3.5
- **Base de Datos**: PostgreSQL (Always Data)
- **ORM**: Exposed
- **Serialización**: kotlinx.serialization
- **Autenticación**: JWT
- **IA**: Google Gemini 1.5 Flash
- **Despliegue**: Railway (plan gratuito)

---

## 📡 Endpoints Principales

### Autenticación
```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
```

### Asistente Comercial
```
POST /api/comercial/chat
Body: { "message": "¿Qué incluye el plan Emprendedor Pro?" }
Response: { "response": "El plan incluye...", "success": true }
```

### Asistente Operativo
```
POST /api/saas/chat
Headers: { "Authorization": "Bearer <token>" }
Body: { "message": "¿Cuánto stock hay de Café Frío?" }
Response: { "response": "Hay 26 unidades...", "success": true }
```

### Productos (SaaS)
```
GET /api/saas/productos
GET /api/saas/productos/{id}
POST /api/saas/productos
PUT /api/saas/productos/{id}
DELETE /api/saas/productos/{id}
```

### Stock (SaaS)
```
GET /api/saas/stock?local_id={id}
GET /api/saas/stock/{producto_id}/{local_id}
POST /api/saas/stock (agregar/reducir stock)
```

### Planes (Comercial)
```
GET /api/comercial/planes
GET /api/comercial/planes/{id}
```

### Suscripciones (Comercial)
```
GET /api/comercial/suscripciones (solo del usuario autenticado)
POST /api/comercial/suscripciones (crear suscripción)
```

---

## 🔐 Seguridad y Permisos

### Autenticación JWT

- Tokens JWT para autenticación
- Refresh tokens para renovación
- Tokens expiran después de X tiempo

### Validación de Permisos

**En cada endpoint del SaaS**:
1. Verificar token JWT válido
2. Verificar suscripción activa
3. Verificar rol del usuario
4. Aplicar filtros según rol (OPERADOR solo ve sus locales)

**Ejemplo de validación**:
```kotlin
fun validateSaaSRequest(user: User): Boolean {
    if (!user.hasActiveSubscription()) {
        throw UnauthorizedException("Suscripción requerida")
    }
    return true
}

fun validateAdminRequest(user: User): Boolean {
    if (user.role != Role.ADMINISTRADOR) {
        throw ForbiddenException("Solo administradores")
    }
    return true
}
```

---

## 🗄️ Configuración de Base de Datos

### Credenciales Always Data

Las credenciales se configuran en variables de entorno:

```env
DATABASE_URL=jdbc:postgresql://postgresql-[usuario].alwaysdata.net:5432/siga_db
DB_USER=tu_usuario_alwaysdata
DB_PASSWORD=tu_password_alwaysdata
```

### Scripts de Base de Datos

Crear scripts SQL para:
1. Crear esquemas (`siga_saas`, `siga_comercial`)
2. Crear tablas según modelo ER
3. Insertar datos iniciales (planes, categorías, etc.)

**Ubicación sugerida**: `src/main/resources/db/migrations/`

---

## 🤖 Integración con Gemini

### API Key

La API key de Gemini se configura en variables de entorno:
```env
GEMINI_API_KEY=tu_api_key_gemini_aqui
```

### Contexto SIGA para Gemini

Cada asistente tiene un contexto específico que se envía a Gemini:

**Asistente Comercial**:
```
Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
Tu función es ayudar a usuarios interesados en conocer los planes y características de SIGA.
Responde sobre planes, precios, características, trial gratuito.
Si preguntan sobre inventario, redirige a app.siga.com.
```

**Asistente Operativo**:
```
Eres SIGA, el asistente virtual del Sistema Inteligente de Gestión de Activos.
Ayudas a usuarios a gestionar su inventario, consultar stock, ver ventas, etc.
Responde de forma amigable y profesional en español.
Usa los datos proporcionados en el contexto para dar respuestas precisas.
```

### Implementación RAG

```kotlin
class OperationalAssistantService {
    suspend fun buildRAGContext(userId: String, query: String, role: Role): String {
        val context = mutableListOf<String>()
        
        // 1. Datos de inventario (según rol)
        if (role == Role.ADMINISTRADOR) {
            context.add(getAllInventoryData(userId))
        } else {
            context.add(getAssignedLocationsInventory(userId))
        }
        
        // 2. Datos comerciales (solo ADMIN)
        if (role == Role.ADMINISTRADOR && isCommercialQuery(query)) {
            context.add(getUserPlan(userId))
        }
        
        return context.joinToString("\n")
    }
    
    suspend fun sendMessage(userId: String, message: String, role: Role): String {
        val ragContext = buildRAGContext(userId, message, role)
        val prompt = "$sigaContext\n\n$ragContext\n\nUsuario: $message\n\nSIGA:"
        return geminiService.generate(prompt)
    }
}
```

---

## 🚀 Despliegue

### Railway

1. Conectar repositorio GitHub
2. Configurar variables de entorno
3. Build command: `./gradlew build`
4. Start command: `./gradlew run`
5. URL generada: `https://siga-api.railway.app`

### Variables de Entorno en Railway

```env
DATABASE_URL=jdbc:postgresql://...
DB_USER=...
DB_PASSWORD=...
GEMINI_API_KEY=...
JWT_SECRET=...
ALLOWED_ORIGINS=https://siga-prototipo.vercel.app,https://siga.com
```

---

## 📋 Checklist de Desarrollo

### Fase 1: Configuración Inicial
- [ ] Proyecto Ktor creado
- [ ] Dependencias configuradas
- [ ] Estructura de carpetas creada
- [ ] Variables de entorno configuradas
- [ ] Conexión a PostgreSQL funcionando

### Fase 2: Base de Datos
- [ ] Scripts de creación de esquemas
- [ ] Scripts de creación de tablas
- [ ] Modelos Exposed creados
- [ ] Migraciones funcionando

### Fase 3: Autenticación
- [ ] Endpoint de login
- [ ] Endpoint de registro
- [ ] JWT tokens funcionando
- [ ] Validación de tokens en endpoints protegidos

### Fase 4: Asistentes IA
- [ ] GeminiService implementado
- [ ] CommercialAssistantService implementado
- [ ] OperationalAssistantService implementado
- [ ] RAG funcionando
- [ ] Validación de permisos por rol

### Fase 5: Endpoints CRUD
- [ ] Productos (GET, POST, PUT, DELETE)
- [ ] Stock (GET, POST para agregar/reducir)
- [ ] Ventas (GET, POST)
- [ ] Planes (GET)
- [ ] Suscripciones (GET, POST)

### Fase 6: Testing y Despliegue
- [ ] Tests unitarios
- [ ] Tests de integración
- [ ] Despliegue en Railway
- [ ] Verificar conexión desde frontends

---

## 📚 Recursos y Referencias

### Documentación
- [Ktor Documentation](https://ktor.io/docs/)
- [Exposed ORM](https://github.com/JetBrains/Exposed)
- [PostgreSQL JDBC](https://jdbc.postgresql.org/)
- [Google Generative AI](https://ai.google.dev/docs)

### Archivos de Referencia
- `ECOSISTEMA_SIGA.md` - Documentación completa del ecosistema
- `MODULO_POS.md` - **Documentación del módulo POS (LEER PRIMERO)**
- `GUIA_INTELLIJ_IDEA.md` - Guía de configuración
- Modelo ER en `/docs` del repositorio principal

### ⚠️ IMPORTANTE: Módulo POS

**El módulo POS es parte integral del sistema**. Ver `MODULO_POS.md` para:
- Estructura de tablas (TURNOS_CAJA, TRANSACCIONES_POS, etc.)
- Descuento automático de inventario al realizar venta
- Endpoints del POS
- Integración con el asistente
- Nuevo rol: CAJERO

**No es un sistema separado, es parte del mismo backend y schema `siga_saas`**.

---

## 🎯 Objetivo Final

Crear un backend robusto que:
- ✅ Alimente a los 3 frontends (web comercial, prototipo, app Android)
- ✅ Maneje autenticación y permisos correctamente
- ✅ Proporcione asistentes IA funcionales con RAG
- ✅ Sea escalable y mantenible
- ✅ Esté desplegado y funcionando en Railway

---

## ⚠️ Puntos Críticos a Recordar

1. **Dos esquemas separados**: `siga_saas` y `siga_comercial`
2. **Dos asistentes diferentes**: Comercial (público) y Operativo (autenticado)
3. **Permisos por rol**: ADMINISTRADOR vs OPERADOR
4. **RAG contextual**: Construir contexto según pregunta y rol
5. **Validación de suscripción**: Requerida para endpoints del SaaS
6. **CORS configurado**: Permitir requests desde Vercel y apps móviles

---

**¡Éxito con el desarrollo!** 🚀

