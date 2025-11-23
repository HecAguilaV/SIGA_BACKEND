# 🚀 Ecosistema SIGA - Documentación Oficial

> **Sistema Inteligente de Gestión de Activos**  
> Para que nunca te detengas. • No gestiones tu Inventario, Gestiona tu Tiempo.

---

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura del Ecosistema](#arquitectura-del-ecosistema)
3. [Estructura de Repositorios](#estructura-de-repositorios)
4. [Arquitectura de Base de Datos](#arquitectura-de-base-de-datos)
5. [Arquitectura de Asistentes IA](#arquitectura-de-asistentes-ia)
6. [Permisos y Roles](#permisos-y-roles)
7. [Stack Tecnológico](#stack-tecnológico)
8. [Despliegue e Infraestructura](#despliegue-e-infraestructura)
9. [Flujos de Usuario](#flujos-de-usuario)
10. [Decisiones de Arquitectura](#decisiones-de-arquitectura)

---

## 🎯 Visión General

SIGA es un **mini ERP para PYMES chilenas** con simplicidad radical y asistente conversacional inteligente.

### La Problemática

> Para las PYMES, la gestión de inventario no es un trámite, es una **parálisis operativa**.

El emprendedor, que cumple múltiples roles, vive detenido por tareas manuales en sistemas complejos o planillas caóticas no diseñadas para la movilidad. Esta fricción constante causa:

- **Pérdidas económicas directas**: Quiebres de stock (ventas perdidas) y mermas (capital desperdiciado)
- **Pérdida de tiempo**: El activo más valioso del negocio

**La oportunidad**: Servir a la gran mayoría de PYMES abrumadas por la complejidad de los ERPs actuales, ofreciendo una solución que trabaje para ellas.

### Filosofía de SIGA

> **Que el emprendedor nunca se detenga.**

SIGA nace de la experiencia real: mientras cargas tu furgón de reparto, puedes decirle al asistente qué hacer. No necesitas estar sentado en un escritorio gestionando esto y aquello.

### Propuesta de Valor

> **No gestiones tu Inventario, Gestiona tu Tiempo.**

### Los Tres Pilares de SIGA

SIGA devuelve el control mediante tres pilares clave:

1. **Asistente Conversacional (Chatbot)**
   - Actualizar inventario, consultar y generar reportes en lenguaje natural
   - Desde cualquier dispositivo (móvil y web)
   - Resolver en segundos tareas que hoy toman minutos
   - CRUD completo mediante lenguaje natural
   - Si un producto no existe, el asistente lo crea automáticamente

2. **Inteligencia Proactiva con IA**
   - Modelos de lenguaje (Gemini) conectados directamente a datos en tiempo real
   - Anticipar quiebres de stock
   - Sugerir compras
   - Alertar sobre anomalías
   - Generar gráficos y análisis con insights claros

3. **Simplicidad Radical**
   - Interfaz limpia y clara
   - La IA no solo genera reportes, los explica en texto simple
   - Insights accionables, no datos abrumadores
   - Sin tecnicismos, respuestas claras

### Componentes del Ecosistema

- **Portal Comercial** (siga.com): Marketing y venta de suscripciones
- **Aplicación SaaS** (app.siga.com): Sistema operativo de gestión
- **Aplicación Móvil Android**: Acceso nativo desde dispositivos móviles
- **Módulo POS** (Point of Sale): Sistema de ventas físicas integrado
- **Backend Unificado**: API REST que alimenta todos los componentes

### Roadmap de Módulos

**Fase 1 (Actual)**: Gestión de Inventario
- ✅ Productos, categorías, locales
- ✅ Stock y movimientos
- ✅ Asistente conversacional con CRUD completo

**Fase 2 (Próximo)**: Módulo POS
- 🔄 Sistema de ventas físicas
- 🔄 Descuento automático de inventario
- 🔄 Registro de ventas en tiempo real

**Fase 3 (Futuro)**: Módulos Adicionales
- 📋 Contabilidad
- 👥 RRHH
- 📊 Reportes avanzados
- 🔗 Integraciones

### Principios de Diseño

**Fundamentales (Lo Más Importante)**:

1. **Simplicidad Radical** ⭐
   - La simplicidad y la experiencia del usuario final son lo más básico y fundamental
   - Interfaz clara, asistente inteligente, sin fricción
   - Sin tecnicismos, respuestas claras y accionables
   - El asistente SIGA cobra relevancia por llevar la simplicidad a un nivel nunca visto en ERPs

2. **Experiencia de Usuario Primero** ⭐
   - Cada decisión técnica debe servir a la experiencia del usuario
   - Si algo es complejo para el usuario, no es la solución correcta
   - El asistente conversacional es la clave para simplificar operaciones complejas

**Técnicos**:

- **Backend Único**: Una sola API para todos los clientes
- **Base de Datos Centralizada**: PostgreSQL con esquemas separados por contexto
- **Asistentes Especializados**: Dos asistentes IA con contextos distintos
- **Seguridad por Capas**: Permisos granulares por rol y contexto
- **Movilidad Primero**: Operar desde cualquier lugar, no solo desde escritorio

### Diferenciador Clave

> **Ningún ERP actual tiene este enfoque de simplicidad radical mediante asistente conversacional.**

Mientras otros agregan IA como "feature", SIGA la convierte en el **núcleo de la experiencia**, permitiendo operar el sistema completo mediante lenguaje natural. Esto nace de haber vivido el dolor real en el campo operativo.

---

## 🏗️ Arquitectura del Ecosistema

```
┌─────────────────────────────────────────────────────────────┐
│                    Ecosistema SIGA                           │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   siga.com   │  │ app.siga.com │  │ App Android  │     │
│  │  (Comercial) │  │  (Prototipo) │  │   (Nativa)   │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │             │
│         └──────────────────┴──────────────────┘             │
│                            │                                │
│                    ┌───────▼────────┐                       │
│                    │  Backend Ktor  │                       │
│                    │  (Railway)     │                       │
│                    └───────┬────────┘                       │
│                            │                                │
│                    ┌───────▼────────┐                       │
│                    │  PostgreSQL    │                       │
│                    │  (Always Data) │                       │
│                    └────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de Repositorios

### Tipo: **Polyrepo (Repositorios Separados)**

Cada componente tiene su propio repositorio para:
- ✅ Despliegues independientes
- ✅ Mantenimiento separado
- ✅ Menos conflictos en equipo
- ✅ Escalabilidad individual

### Repositorios del Ecosistema

```
SIGA_Backend/              ← Backend Ktor + PostgreSQL
├── src/
├── build.gradle.kts
└── README.md

SIGA_Mobile/               ← App Android (DevAppMobile)
├── app/
├── build.gradle.kts
└── README.md

SIGA_Web_Prototipo/        ← Prototipo SvelteKit
├── src/
├── package.json
└── README.md

SIGA_Web_Comercial/        ← Web Comercial React
├── src/
├── package.json
└── README.md
```

---

## 🗄️ Arquitectura de Base de Datos

### Una Base de Datos, Dos Esquemas

**PostgreSQL en Always Data** con separación lógica por contexto de negocio.

#### Esquema: `siga_saas`
**Propósito**: Sistema operativo de gestión de inventario y ventas

**Entidades Principales**:

**Gestión de Inventario**:
- `USUARIOS` - Usuarios operativos (ADMINISTRADOR, OPERADOR, CAJERO)
- `PRODUCTOS` - Catálogo de productos
- `CATEGORIAS` - Categorías de productos
- `LOCALES` - Bodegas/sucursales
- `STOCK` - Stock por producto y local
- `MOVIMIENTOS` - Historial de movimientos de stock
- `ALERTAS` - Alertas y notificaciones

**Módulo POS (Point of Sale)**:
- `VENTAS` - Registro de ventas (desde POS y manuales)
- `DETALLES_VENTA` - Detalles de cada venta (productos vendidos)
- `TRANSACCIONES_POS` - Transacciones del punto de venta
- `METODOS_PAGO` - Métodos de pago (efectivo, tarjeta, transferencia)
- `TURNOS_CAJA` - Turnos de caja por local y usuario

**Consumido por**:
- Prototipo Web (app.siga.com)
- App Android
- Módulo POS (futuro)

#### Esquema: `siga_comercial`
**Propósito**: Portal comercial y gestión de suscripciones

**Entidades Principales**:
- `USUARIOS` - Clientes del portal
- `PLANES` - Planes de suscripción disponibles
- `SUSCRIPCIONES` - Suscripciones activas de clientes
- `PAGOS` - Registro de pagos
- `FACTURAS` - Facturas generadas
- `CARRITOS` - Carritos de compra

**Consumido por**:
- Web Comercial (siga.com)

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

#### 1. Asistente Comercial (siga.com)

**Endpoint**: `POST /api/comercial/chat`

**Propósito**: Ventas, marketing, soporte pre-venta

**Acceso a Datos**:
- ✅ `siga_comercial.*` (planes, precios, características)
- ❌ `siga_saas.*` (sin acceso a inventario)

**Usuarios**:
- Público (visitantes)
- Clientes (con/sin suscripción activa)

**Puede Responder**:
- "¿Qué incluye el plan Emprendedor Pro?"
- "¿Cuánto cuesta el plan Crecimiento?"
- "¿Hay trial gratuito?"
- "¿Cómo funciona SIGA?"

**No Puede Responder**:
- Preguntas sobre inventario → Redirige a app.siga.com

**RAG Context**:
```kotlin
{
    siga_comercial: {
        planes: todos los planes disponibles,
        suscripcion: solo si el usuario está autenticado
    }
}
```

#### 2. Asistente Operativo (app.siga.com + App Android)

**Endpoint**: `POST /api/saas/chat`

**Propósito**: Operaciones del negocio

**Acceso a Datos**:
- ✅ `siga_saas.*` (inventario completo según rol)
- ✅ `siga_comercial.SUSCRIPCIONES` (solo su suscripción, según rol)

**Requisitos**:
- Autenticación JWT
- Suscripción activa

**RAG Context por Rol**:

**ADMINISTRADOR**:
```kotlin
{
    siga_saas: {
        productos: todos,
        stock: todos los locales,
        ventas: todas,
        movimientos: todos
    },
    siga_comercial: {
        suscripcion: completa,
        plan: detalles completos,
        facturacion: historial
    }
}
```

**OPERADOR**:
```kotlin
{
    siga_saas: {
        productos: todos (solo lectura),
        stock: solo locales asignados,
        ventas: solo las que registró,
        movimientos: solo de sus locales
    },
    siga_comercial: {
        // SIN ACCESO
        // Si pregunta sobre plan → Rechazar
    }
}
```

**Puede Responder (ADMIN)**:
- "¿Cuánto stock hay de Café Frío?"
- "¿Qué plan tengo contratado?"
- "¿Cuántas bodegas me permite mi plan?"

**Puede Responder (OPERADOR)**:
- "¿Cuánto stock hay de Café Frío?" (solo sus locales)
- "Agrega 10 unidades de Leche Chocolate"

**No Puede Responder (OPERADOR)**:
- Preguntas sobre plan → "No tienes permisos. Contacta al administrador"

---

## 🔐 Permisos y Roles

### Roles en el Sistema

#### ADMINISTRADOR
**Contexto**: Usuario principal de la empresa

**Permisos**:
- ✅ Acceso completo a inventario (todos los locales)
- ✅ Consultar plan, suscripción, precios, límites
- ✅ Ver facturación y pagos
- ✅ Gestionar usuarios operadores
- ✅ Configuración del sistema

**Ejemplos de Consultas Permitidas**:
- "¿Qué plan tengo contratado?"
- "¿Cuántas bodegas me permite mi plan?"
- "¿Cuándo vence mi suscripción?"
- "¿Cuánto pago mensualmente?"

#### OPERADOR/VENDEDOR
**Contexto**: Usuario operativo de la empresa

**Permisos**:
- ✅ Consultar inventario (solo locales asignados)
- ✅ Ingresar mercadería
- ✅ Registrar ventas
- ✅ Ver movimientos de sus locales
- ❌ **NO** puede ver planes, precios, costos, límites

**Ejemplos de Consultas Permitidas**:
- "¿Cuánto stock hay de Café Frío en ITR?"
- "¿Qué productos tienen stock bajo?"
- "Agrega 10 unidades de Leche Chocolate"

**Ejemplos de Consultas Bloqueadas**:
- "¿Qué plan tiene la empresa?" → ❌ "No tienes permisos"
- "¿Cuánto cuesta la suscripción?" → ❌ "Contacta al administrador"

### Matriz de Permisos

| Consulta | ADMINISTRADOR | OPERADOR |
|----------|---------------|----------|
| Stock de productos | ✅ Todos los locales | ✅ Solo sus locales |
| Ingresar mercadería | ✅ Sí | ✅ Sí (solo sus locales) |
| Ver ventas | ✅ Todas | ✅ Solo las suyas |
| Consultar plan | ✅ Sí | ❌ No |
| Ver precios | ✅ Sí | ❌ No |
| Ver límites | ✅ Sí | ❌ No |

---

## 🛠️ Stack Tecnológico

### Backend
- **Lenguaje**: Kotlin
- **Framework**: Ktor
- **Base de Datos**: PostgreSQL (Always Data)
- **ORM**: Exposed (recomendado) o Ktorm
- **Serialización**: kotlinx.serialization
- **Autenticación**: JWT
- **IA**: Google Gemini 1.5 Flash (RAG)

### Frontend Web
- **Prototipo**: SvelteKit 5
- **Comercial**: React 18
- **Build Tool**: Vite
- **Despliegue**: Vercel

### Mobile
- **Android**: Kotlin + Jetpack Compose
- **Futuro iOS**: Kotlin Multiplatform (KMM) + SwiftUI

### Infraestructura
- **Base de Datos**: Always Data (PostgreSQL)
- **Backend**: Railway (plan gratuito)
- **Frontend**: Vercel
- **CDN**: Vercel Edge Network

---

## 🚀 Despliegue e Infraestructura

### Arquitectura de Despliegue

```
┌─────────────────────────────────────────┐
│         Always Data                     │
│  PostgreSQL: postgresql-[user].alwaysdata.net│
│  - Esquema: siga_saas                   │
│  - Esquema: siga_comercial              │
└─────────────────────────────────────────┘
                    ↑
                    │ JDBC
                    │
┌─────────────────────────────────────────┐
│    Backend Ktor (Railway)               │
│    https://siga-api.railway.app         │
│                                         │
│    Endpoints:                           │
│    - POST /api/comercial/chat          │
│    - POST /api/saas/chat                │
│    - GET  /api/productos                │
│    - GET  /api/stock                    │
│    - POST /api/auth/login               │
└─────────────────────────────────────────┘
                    ↑
                    │ HTTPS
        ┌───────────┼───────────┐
        │           │           │
┌───────▼───┐ ┌────▼────┐ ┌────▼─────┐
│  Vercel   │ │ Vercel │ │  App     │
│ Prototipo │ │Comercial│ │ Android │
│ SvelteKit │ │ React  │ │ Kotlin   │
└───────────┘ └─────────┘ └──────────┘
```

### Variables de Entorno

#### Backend (Railway)
```env
# Base de Datos
DATABASE_URL=jdbc:postgresql://postgresql-[user].alwaysdata.net:5432/siga_db
DB_USER=tu_usuario
DB_PASSWORD=tu_password

# API Keys
GEMINI_API_KEY=tu_api_key_gemini

# JWT
JWT_SECRET=tu_secret_key

# CORS
ALLOWED_ORIGINS=https://siga-prototipo.vercel.app,https://siga.com,https://app.siga.com
```

#### Frontend Web (Vercel)
```env
# Prototipo (SvelteKit)
VITE_API_URL=https://siga-api.railway.app

# Comercial (React)
REACT_APP_API_URL=https://siga-api.railway.app
```

#### App Android
```kotlin
// BuildConfig
API_BASE_URL=https://siga-api.railway.app
```

---

## 🔄 Flujos de Usuario

### Flujo 1: Visitante en siga.com
```
Usuario pregunta: "¿Cuánto stock tengo?"
    ↓
Asistente Comercial detecta pregunta de inventario
    ↓
Responde: "Para consultar tu inventario, necesitas una suscripción activa. 
          Regístrate aquí y obtén acceso a app.siga.com"
```

### Flujo 2: Cliente con suscripción en siga.com
```
Usuario pregunta: "¿Cuánto stock tengo?"
    ↓
Asistente Comercial detecta pregunta de inventario
    ↓
Responde: "Para consultar tu inventario en tiempo real, accede a 
          app.siga.com o usa la app móvil. Aquí puedes gestionar 
          tu suscripción y ver tus facturas."
```

### Flujo 3: Administrador en app.siga.com
```
Usuario pregunta: "¿Qué plan tengo?"
    ↓
Asistente Operativo verifica: rol = ADMINISTRADOR
    ↓
RAG consulta siga_comercial.SUSCRIPCIONES
    ↓
Responde: "Tienes el plan Emprendedor Pro activo hasta el 15/01/2025. 
          Incluye 2 bodegas (actualmente usas: ITR y Presidente Ibáñez) 
          y 3 usuarios (tienes 2 activos). Precio: 0.9 UF mensual."
```

### Flujo 4: Operador en app.siga.com
```
Usuario pregunta: "¿Qué plan tiene la empresa?"
    ↓
Asistente Operativo verifica: rol = OPERADOR
    ↓
Responde: "No tienes permisos para consultar información del plan o 
          suscripción. Para esta información, contacta al administrador 
          de tu cuenta."
```

### Flujo 5: Operador en app.siga.com
```
Usuario pregunta: "¿Cuánto stock hay de Café Frío?"
    ↓
Asistente Operativo verifica: rol = OPERADOR
    ↓
RAG consulta siga_saas.STOCK (solo locales asignados al operador)
    ↓
Responde: "Café Frío Listo 350ml: 26 unidades en ITR"
```

---

## 🏛️ Decisiones de Arquitectura

### 1. Polyrepo vs Monorepo
**Decisión**: Polyrepo (repositorios separados)

**Razón**: 
- Despliegues independientes
- Mantenimiento separado
- Menos conflictos en equipo
- Escalabilidad individual

### 2. Base de Datos: Una BD, Dos Esquemas
**Decisión**: PostgreSQL con esquemas `siga_saas` y `siga_comercial`

**Razón**:
- Separación lógica clara
- Una sola conexión
- Fácil de mantener
- Posibilidad de relaciones entre esquemas

### 3. Backend: Monolito vs Microservicios
**Decisión**: **Monolito Modular** (inicial)

**Razón**:
- Menor complejidad inicial
- Más fácil de desarrollar y mantener
- Suficiente para MVP
- Puede evolucionar a microservicios después

**Estructura Modular**:
```
Backend (Monolito)
├── Módulo: Comercial (planes, suscripciones)
├── Módulo: SaaS (inventario, stock)
├── Módulo: Autenticación
└── Módulo: Asistentes IA
```

**Futuro**: Si crece, se puede dividir en microservicios:
- `siga-comercial-service`
- `siga-saas-service`
- `siga-assistant-service`
- `siga-auth-service`

### 4. Asistentes: Dos Asistentes Separados
**Decisión**: Asistente Comercial + Asistente Operativo

**Razón**:
- Separación de responsabilidades
- Seguridad por contexto
- Mejor experiencia de usuario
- RAG más eficiente

### 5. Despliegue: Railway para Backend
**Decisión**: Railway (plan gratuito)

**Razón**:
- Soporte nativo para JVM/Kotlin
- Despliegue desde GitHub
- Variables de entorno fáciles
- HTTPS automático
- Plan gratuito suficiente para MVP

---

## 📚 Documentación Adicional

- [README Principal del Backend](../SIGA_Backend/README.md) - Guía de desarrollo
- [Guía de Configuración IntelliJ IDEA](GUIA_INTELLIJ_IDEA.md) - Setup inicial
- [Guía de Seguridad](SECURITY.md) - Mejores prácticas
- [Modelo de Datos](../docs/diagrams/entidad_relacion.svg) - ER Diagram

---

## 🎯 Roadmap

### Fase 1: MVP (Actual)
- ✅ Arquitectura definida
- ✅ Base de datos diseñada
- ✅ Frontends funcionando (datos hardcodeados)
- 🔄 Backend en desarrollo

### Fase 2: Integración
- [ ] Backend completo
- [ ] Conexión frontends → backend
- [ ] Autenticación JWT
- [ ] Asistentes IA funcionando

### Fase 3: Producción
- [ ] Testing completo
- [ ] Optimizaciones
- [ ] Monitoreo
- [ ] Documentación API

### Fase 4: Escalabilidad
- [ ] Evaluar microservicios (si es necesario)
- [ ] Caché y optimizaciones
- [ ] CDN para assets
- [ ] App iOS

---

## 👥 Contexto del Proyecto

SIGA es un proyecto desarrollado por un **estudiante de Ingeniería Informática de Duoc UC**, transformando la teoría del aula en una solución real.

### Origen: Experiencia Real en el Campo

> **SIGA nació del dolor real vivido en el campo operativo.**

La chispa inicial nació de la experiencia personal como operario, viviendo de primera mano la frustración de sistemas complejos y lentos. Mientras cargaba el furgón de reparto, pensaba: "¿Por qué no puedo simplemente decirle al sistema qué hacer?"

**Muchos hablan de IA en sus ERP, pero nadie ha vivido el dolor en el campo operativo como el creador de SIGA.**

Esta experiencia única es lo que diferencia a SIGA: no es teoría aplicada, es **dolor real convertido en solución**.

### Búsqueda de Equipo

Actualmente SIGA es un proyecto individual, pero está en búsqueda activa de:
- **Cofundador técnico o comercial** que pueda sumergirse en la visión
- **Colaboradores** que compartan la pasión por simplificar la gestión de inventarios

> Este es un proyecto ambicioso que requiere equipo. Si esta visión resuena contigo, este es el momento de unirse.

### Propósito del Proyecto

SIGA tiene múltiples objetivos:

1. **Portafolio**: Demostrar capacidades técnicas y visión de producto
2. **Capstone**: Proyecto de título para Ingeniería Informática
3. **Startup Chile**: Aspiración a postular para financiamiento y aceleración
4. **SIGA SpA**: Visión a largo plazo de crear la empresa

### Áreas de Conocimiento Aplicadas

- **Ingeniería de Software**: Principios de diseño aplicados a arquitectura SaaS escalable
- **Gestión de Datos**: Modelado de base de datos relacional eficiente
- **Inteligencia Artificial Aplicada**: Conectar modelos de lenguaje con datos en tiempo real
- **Desarrollo y UX**: Construir interfaz simple y rápida, enfocada en eficiencia

SIGA es el campo de pruebas, el portafolio y el primer paso para demostrar de lo que es capaz un estudiante con visión clara y experiencia real.

---

## 👥 Contribución

Este es un proyecto en desarrollo activo. Para contribuir:

1. Revisa la arquitectura en este documento
2. Sigue los principios de diseño establecidos
3. Mantén la separación de contextos
4. Respeta los permisos por rol
5. Recuerda la filosofía: "Que el emprendedor nunca se detenga"

---

## 📄 Licencia

Este proyecto está bajo la **Licencia MIT**.

**Aplicación**: Todos los repositorios del ecosistema SIGA deben tener la misma licencia MIT para mantener consistencia y permitir uso libre con atribución.

**Repositorios con Licencia MIT**:
- SIGA_Backend
- SIGA_Mobile (DevAppMobile)
- SIGA_Web_Prototipo
- SIGA_Web_Comercial

**¿Por qué MIT?**
- Permite uso comercial y no comercial
- Permite modificación
- Requiere solo atribución
- Ideal para proyectos open source y portafolio
- Compatible con futura creación de SIGA SpA

---

## 🚀 Roadmap Personal y Aspiraciones

### Objetivos Inmediatos
- ✅ Completar MVP funcional
- 🔄 Desarrollar backend completo
- 🔄 Integrar todos los componentes
- 📋 Presentar como Capstone

### Objetivos a Mediano Plazo
- 📋 Postular a Startup Chile
- 📋 Encontrar cofundador o equipo
- 📋 Validar con primeros clientes beta
- 📋 Crear SIGA SpA

### Visión a Largo Plazo
- 🌟 Convertir SIGA en el ERP más simple para PYMES chilenas
- 🌟 Expandir a otros países de Latinoamérica
- 🌟 Agregar módulos: Contabilidad, RRHH, etc.
- 🌟 Construir un ecosistema completo de gestión empresarial

---

**Última actualización**: Diciembre 2024  
**Versión del documento**: 1.1  
**Estado**: Arquitectura definida, desarrollo en progreso  
**Desarrollador**: Proyecto individual en búsqueda de equipo

---

> "El tiempo es la moneda" - SIGA  
> "No gestiones tu Inventario, Gestiona tu Tiempo"  
> 
> *Nacido del dolor real en el campo operativo. Construido con simplicidad radical.*

