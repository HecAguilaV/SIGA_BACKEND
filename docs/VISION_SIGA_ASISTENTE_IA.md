# Visión SIGA – El Asistente IA como Corazón del Sistema

**Fecha:** 15 de diciembre de 2025  
**Propósito:** Definir el rol transversal del asistente SIGA en el ecosistema y las capacidades mínimas que debe mantener.

## 1. Declaración de Visión

> "SIGA reduce la fricción operativa traduciendo la intención del negocio en acciones automáticas sobre el inventario y las ventas."

El asistente IA es el orquestador de esa visión: conecta lenguaje natural con operaciones CRUD y analíticas del backend.

## 2. Capacidades Esenciales

### 2.1 Operaciones CRUD
- **Productos:** alta, edición, eliminación y consulta contextual.
- **Stock:** ajustes por local, alertas de mínimos y creación de stock inicial.
- **Ventas:** registro guiado y consulta de métricas de desempeño.
- **Locales y categorías:** gestión de catálogos operativos.

### 2.2 Inteligencia Operativa
- Identificar quiebres de stock y sugerir reabastecimiento.
- Detectar productos con baja rotación y proponer acciones.
- Simular impacto de cambios de precio.

### 2.3 Integración Multicanal
- Disponible en WebApp (modal asistente) y App Móvil (comandos de voz/texto).
- Interfaz comercial separada (`/api/comercial/chat`) para orientación de planes y captación.

## 3. Arquitectura de Alto Nivel

```
Cliente → Gateway SIGA → Servicio de Orquestación IA → Controladores REST → Servicio de Dominio → PostgreSQL
```

- **Orquestación IA:** traduce prompts, invoca OpenAI/Gemini y mapea la intención a comandos internos.
- **Controladores REST:** reutilizan los mismos endpoints expuestos a clientes humanos para garantizar consistencia.
- **Auditoría:** todos los comandos generados por IA se registran con `origen=IA`.

## 4. Experiencia de Usuario

- Respuestas accionables: cada mensaje incluye confirmación, resumen y próximos pasos sugeridos.
- Fallback seguro: si la intención no es reconocida, propone alternativas documentadas.
- Aprendizaje continuo: se nutre de logs etiquetados por los líderes de equipo para mejorar prompts y flujos.

## 5. Gobernanza y Seguridad

- Permisos finos: la IA respeta los mismos scopes que el usuario autenticado.
- Controles antifraude: límites de frecuencia, validación de montos y doble confirmación en operaciones críticas.
- Observabilidad: métricas en Grafana (tasa de éxito, latencia, operaciones IA vs humanas).

---

## Autor

> **Héctor Aguila**  
>Un Soñador con Poca RAM