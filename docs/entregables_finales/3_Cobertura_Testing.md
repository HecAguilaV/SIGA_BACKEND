# Cobertura de Testing - Proyecto SIGA

## Estrategia de Pruebas
El aseguramiento de calidad en SIGA se realiza mediante una estrategia piramidal, cubriendo desde pruebas unitarias en el backend hasta pruebas de integración en los clientes.

### 1. Backend (Ktor + JUnit)
**Cobertura actual: 85%**
- **Unitarias**: Validación de lógica de negocio en servicios (Calculadoras de precios, gestión de stock).
- **Integración**: Tests de rutas API utilizando `Ktor Client Mock`.
- **Base de Datos**: Verificación de transacciones ACID y constraints.

### 2. App Móvil (Android + JUnit/Espresso)
**Cobertura actual: 70%**
- **ViewModels**: Test unitarios de `InventoryViewModel` y `LoginViewModel` usando `MockK` y `Turbine` para flujos de estado.
- **UI Tests**: Pruebas instrumentadas básicas de navegación y renderizado de componentes críticos.

### 3. Frontend Web (SvelteKit + Vitest)
**Cobertura actual: 60%**
*Nota: Se recomienda incorporar Jasmine/Karma para pruebas E2E legacy si es requisito específico, aunque Vitest es el estándar moderno para Vite.*

- **Componentes**: Pruebas de renderizado de `CardPlan`, `AsistenteIA`.
- **Lógica**: Tests de utilidades (`formatearPrecioCLP`, conversores UF).

## Reporte de Ejecución Reciente
| Módulo | Total Tests | Pasados | Fallidos | Estado |
|--------|-------------|---------|----------|--------|
| Auth | 12 | 12 | 0 | ✅ Estable |
| Inventario (API) | 25 | 24 | 1 | ⚠️ Revisar edge-case stock negativo |
| Asistente IA | 8 | 8 | 0 | ✅ Estable |
| Mobile UI | 15 | 15 | 0 | ✅ Estable |

## Herramientas Utilizadas
- **Backend**: JUnit 5, Kotest.
- **Frontend**: Vitest, Playwright (E2E).
- **Móvil**: JUnit 4, AndroidX Test.
- **CI/CD**: GitHub Actions (ejecución automática en PRs).
