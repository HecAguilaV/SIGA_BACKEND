# SIGA-BACKEND: Implementación Agéntica

Este módulo actúa como el motor de inteligencia de SIGA.

## ⚙️ Integración de IA
- **Motor**: Google Gemini (vía SDK de Spring AI o REST).
- **Controladores**: Los endpoints de `/api/chat` y `/api/ai` gestionan el flujo de prompts.

## 📝 Gestión de Prompts
- Los prompts se mantienen en `src/main/resources/prompts/` (cuando sea posible) para facilitar su ajuste sin recompilar.
- Se utiliza **RAG (Retrieval-Augmented Generation)** básico consultando el esquema `siga_saas` para dar respuestas contextualizadas al negocio.

## 🛡️ SafeMode (Fallback)
Cuando la IA excede su cuota o falla:
1.  El servicio detecta la excepción.
2.  Ejecuta llamadas a procedimientos almacenados pre-calculados (`015_ai_fallback_functions.sql`).
3.  Retorna una respuesta estructurada "Safe" al frontend.

## 🚀 Despliegue Agéntico
- Variables de entorno críticas: `GEMINI_API_KEY`.
- Hosting: Render/Railway con soporte para escalado reactivo.
