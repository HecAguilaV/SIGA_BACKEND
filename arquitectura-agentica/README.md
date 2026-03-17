# Arquitectura Agéntica de SIGA

Este directorio contiene los cimientos estructurales y la documentación técnica del ecosistema SIGA, gestionada mediante inteligencia artificial agéntica.

## Metodología: Spec-Driven Development (SDD)

Para garantizar cimientos sólidos y evitar "picar código" sin sentido, seguimos el flujo formal de SDD:

1.  **Exploración (Explore):** Análisis de terreno y requerimientos.
2.  **Propuesta (Proposal):** Anteproyecto técnico y visual.
3.  **Especificación (Spec):** Cálculos estructurales y lógica de negocio (Contratos de datos, lógica PL/SQL).
4.  **Diseño (Design):** Planos detallados de archivos y componentes.
5.  **Tareas (Tasks):** Carta Gantt de implementación.
6.  **Aplicación (Apply):** Construcción efectiva del código.

---

## Memoria del Sistema (Engram)

Utilizamos un sistema de memoria persistente para que las decisiones arquitectónicas no se pierdan entre sesiones. 

**Conceptos Clave:**
- **Inmutabilidad del Tenant:** El `usuario_comercial_id` es la viga maestra del Multi-tenant.
- **Mobile-First Real:** UX pensada para el trabajo en terreno, no solo responsive.
- **Fallback de IA:** La lógica central debe funcionar incluso si las APIs externas fallan (Capa de SQL/PLSQL de respaldo).

---

## Estructura de Documentación

- `ecosistema_siga.md`: Visión general de todos los módulos.
- `guia_sdd.md`: Detalle del proceso de trabajo para nuevos módulos.
- `decisiones_arquitectonicas.md`: Registro de por qué elegimos Kotlin, Svelte y Postgres.

---
> "No construimos castillos de naipes, construimos soluciones para el barro." - Un Soñador con Poca RAM 👨🏻‍💻
