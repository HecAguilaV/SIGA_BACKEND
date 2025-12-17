# Documento de Integración y Arquitectura - SIGA

## 1. Diagrama de Arquitectura
El ecosistema SIGA opera bajo una arquitectura de microservicios lógicos sobre un monorepo backend, servido por una infraestructura cloud distribuida.

```mermaid
graph TD
    UserWeb[Web Comercial] -->|HTTPS| LB[Load Balancer]
    UserApp[Web App SaaS] -->|HTTPS| LB
    UserMob[App Móvil] -->|HTTPS| LB
    
    LB -->|Routing| Backend[SIGA Backend (Ktor)]
    
    subgraph "External Services"
        Gemini[Google Gemini AI]
        DolarAPI[Mindicador.cl]
    end
    
    Backend -->|API Key| Gemini
    Backend -->|REST| DolarAPI
    
    Backend -->|JDBC| DB[(PostgreSQL Cloud)]
```

## 2. Flujos de Integración

### 2.1 Sincronización de Precios (DolarAPI)
- **Frecuencia**: Diaria (Cron job interno).
- **Fallback**: Si la API externa falla, se utiliza el último valor conocido almacenado en caché Redis/Memoria.
- **Uso**: Conversión de planes y reportes financieros multimoneda.

### 2.2 Inteligencia Artificial (Gemini)
- **Modelo**: Gemini 2.5 Pro.
- **Integración**: Vía SDK de Google AI.
- **Patrón**: RAG (Retrieval-Augmented Generation). El backend inyecta contexto del negocio (inventario, ventas) en el prompt del sistema antes de enviar la consulta a Gemini, permitiendo respuestas contextualizadas y precisas.

### 2.3 Single Sign-On (SSO)
- Entre Web Comercial y Web App existe un mecanismo de traspaso de sesión seguro.
- Al hacer clic en "Ir a WebApp" desde el portal comercial, se genera un token de un solo uso (OTP) que la Web App canjea por una sesión JWT válida, evitando re-login.

## 3. Seguridad en Integraciones
- **API Keys**: Rotación mensual, almacenadas en variables de entorno (Railway Secrets).
- **CORS**: Política restrictiva permitiendo solo orígenes verificados (`*.vercel.app`, `localhost:dev`).
- **Rate Limiting**: Protección contra abuso en endpoints públicos.
