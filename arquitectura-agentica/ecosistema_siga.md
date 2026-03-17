# Visión del Ecosistema SIGA

## 1. El Propósito
SIGA nace para solucionar el desfase de inventario en terreno. No es un ERP de escritorio; es una herramienta de movilidad para el "Guerrero Multi-rol".

## 2. Componentes del Sistema

### 🛒 SIGA-BACKEND (El Cerebro)
- **Tecnología:** Kotlin + Spring Boot.
- **Base de Datos:** PostgreSQL (PostGIS para geolocalización futura).
- **Patrón:** Monolito Modular con soporte Multi-tenant estricto.
- **IA:** Integración con Gemini para parsing de lenguaje natural y agentes.

### 📱 SIGA-APP (La Herramienta)
- **Tecnología:** Kotlin Multiplatform / Native.
- **UX:** Online-first con persistencia local (Room) para trabajo en zonas de baja señal.
- **Firma:** Diseño enfocado en rapidez de ingreso (Asistente de Voz/IA).

### 🌐 SIGA-WEBAPP & WEBCOMERCIAL (La Gestión)
- **Tecnología:** Svelte 5 / React.
- **Función:** Registro de clientes, visualización de métricas avanzadas y administración centralizada.

## 3. Principios Arquitectónicos
- **SOLID:** Aplicado especialmente en la Separación de Responsabilidades.
- **Seguridad:** JWT con Refresh Token Interceptor.
- **Escalabilidad:** Diseñado para ser migrado a microservicios satélites si la carga lo requiere.
