# Arquitectura Agéntica: Manual Global de SIGA

Este documento define los principios de colaboración entre el desarrollador, los Agentes de IA (como Antigravity) y el código de SIGA.

## 🤖 El Ecosistema de Agentes
SIGA no es solo código; es un ecosistema vivo. Los agentes operan sobre cuatro pilares:
1.  **Backend (Cerebro)**: Kotlin + Spring Boot procesando lógica compleja.
2.  **Webs (Interfaz)**: React y Svelte facilitando la interacción con el usuario.
3.  **Móvil (Terreno)**: Android capturando datos en tiempo real.
4.  **IA (Asistente)**: Gemini integrando conocimiento en cada paso.

## 🛠️ Metodología SDD (Spec-Driven Development)
Para cualquier cambio importante, seguimos este flujo:
1.  **Planificación**: Definir specs en `arquitectura-agentica/`.
2.  **Ejecución**: Implementación por componentes.
3.  **Verificación**: Pruebas cruzadas entre Web y Backend.

## 🧠 Memoria Compartida
- Utilizamos el sistema de **Engram** para persistir decisiones más allá de la memoria de corto plazo de la sesión actual.
- Cada repositorio contiene su propio `AGENTIC.md` con detalles técnicos de su implementación de IA.

## 🛡️ Fallback y Seguridad
- **Resiliencia**: Siempre existe un SafeMode (funciones SQL) para cuando la API de IA no está disponible.
- **Isolación**: Los agentes operan con credenciales de tenant aisladas para garantizar la seguridad de los datos.

---
*Este manual sirve como ancla para cualquier agente nuevo que entre al proyecto.*
