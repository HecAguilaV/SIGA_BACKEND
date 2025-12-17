# Especificación de Requisitos de Software (ERS) - Ecosistema SIGA

## 1. Introducción
El Sistema Inteligente de Gestión de Activos (SIGA) es una plataforma integral SaaS diseñada para potenciar la operatividad de las PYMEs chilenas, integrando gestión de inventario, ventas y análisis mediante Inteligencia Artificial.

## 2. Descripción General
El sistema se compone de tres módulos principales:
1. **Web Comercial**: Portal de captación y administración de suscripciones.
2. **WebApp (SaaS)**: Sistema operativo central para la gestión del negocio.
3. **App Móvil**: Extensión operativa para trabajo en terreno y control rápido.

## 3. Requisitos Funcionales

### 3.1 Módulo Transversal (Backend & Auth)
- **RF-01**: Autenticación centralizada (SSO) mediante JWT.
- **RF-02**: Gestión de roles (Administrador, Operador, Cajero).
- **RF-03**: API RESTful documentada para comunicación entre clientes.

### 3.2 Web Comercial
- **RF-04**: Landing page con propuesta de valor y video demostrativo.
- **RF-05**: Catálogo público de planes de suscripción (precios en UF/CLP).
- **RF-06**: Simulación de compra y registro de nuevos tenants (empresas).

### 3.3 Web App (Sistema Central)
- **RF-07**: Dashboard analítico con indicadores clave (KPIs).
- **RF-08**: Gestión de inventario (CRUD) con actualización en tiempo real.
- **RF-09**: Asistente con IA (Gemini 2.5) para consultas y operaciones en lenguaje natural.
- **RF-10**: Módulo de Punto de Venta (POS) rápido.

### 3.4 App Móvil
- **RF-11**: Consulta de stock en tiempo real por sucursal.
- **RF-12**: Ajuste de inventario rápido mediante interfaz táctil.
- **RF-13**: Soporte offline-first (persistencia local básica).

## 4. Requisitos No Funcionales
- **RNF-01**: Tiempo de respuesta de API < 200ms.
- **RNF-02**: Disponibilidad del 99.9% (Infraestructura Railway + Vercel).
- **RNF-03**: Diseño responsivo y adaptativo (Glassmorphism UI).
- **RNF-04**: Seguridad en tránsito (HTTPS/TLS) y reposo (Hashing Bcrypt).

## 5. Modelo de Datos
El sistema utiliza una base de datos PostgreSQL particionada lógicamente:
- `public`: Datos compartidos de configuración.
- `comercial`: Gestión de suscripciones y pagos.
- `saas`: Datos operativos de cada tenant (productos, ventas).
