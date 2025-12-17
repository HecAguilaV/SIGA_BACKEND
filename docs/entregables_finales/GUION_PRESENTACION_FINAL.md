# 🎓 GUION SECRETO DE DEFENSA - SIGA

¡Hola! Este documento es tu "chuleta" o guía maestra para la presentación. Aquí tienes los argumentos técnicos exactos basados en el código real de tu proyecto para responder a cada uno de los 8 puntos de la evaluación.

---

## 1. Estructura HTML y Estilos CSS (Web Comercial y Web App)

**Tu argumento:**
"Hemos desarrollado una arquitectura frontend moderna utilizando **React (SIGA-WEBCOMERCIAL)** y **SvelteKit (SIGA-WEBAPP)**. No usamos HTML estático simple, sino **JSX** y componentes reutilizables, lo que garantiza una estructura semántica y mantenible."

**Evidencia en tu código:**
*   **Semántica:** Uso de etiquetas `<header>`, `<main>`, `<section>` y `<footer>` en tus layouts principales.
*   **Estilos:**
    *   Utilizamos **Bootstrap 5** (ver `package.json` línea 23) para la grilla y componentes responsivos rápidos.
    *   Estilos personalizados en CSS moderno (Variables CSS para temas claros/oscuros).
    *   Iconos vectoriales con `phosphor-react`.

---

## 2. Validación de Formularios con JS

**Tu argumento:**
"La validación no se deja solo al navegador. Implementamos una capa de validación lógica en JavaScript antes de enviar datos al servidor, asegurando la integridad de la información crítica como RUTs y teléfonos."

**Evidencia en tu código:**
*   **Archivo clave:** `src/utils/validation.js` en SIGA-WEBCOMERCIAL.
*   **Algoritmo real:** Tienes implementado el **Algoritmo Módulo 11** para validar el RUT chileno.
    *   *Ver función `validarRut(rut)`*: Valida largo, formato y dígito verificador.
    *   *Ver función `validarTelefono(telefono)`*: Asegura formato de 9 dígitos.
*   **UX:** Feedback inmediato al usuario si el formato es incorrecto, evitando peticiones innecesarias al backend.

---

## 3. Trabajo Colaborativo (Git)

**Tu argumento:**
"El proyecto se gestionó mediante control de versiones Git, permitiendo integración continua y trazabilidad de cambios."

**Evidencia:**
*   Existencia de historial de commits (puedes mostrar tu terminal con `git log`).
*   Estructura modular donde Front y Back conviven o se separan en repositorios lógicos.
*   Uso de ramas (branches) para nuevas características (feature branches) antes de fusionar a `main`.

---

## 4. Frontend JS y Responsividad (React/Vite)

**Tu argumento:**
"Para el portal comercial elegimos **React con Vite**. Esta combinación ofrece un *Virtual DOM* para actualizaciones eficientes y una experiencia de usuario fluida (SPA - Single Page Application)."

**Evidencia en tu código:**
*   **Framework:** React 18 (`package.json`).
*   **Gestión de Estado:** Uso de Hooks como `useState` y `useEffect` (ej. en `DashboardScreen.kt` o tus componentes de React).
*   **Responsividad:**
    *   Uso de clases de Bootstrap (`col-md-6`, `d-flex`, `d-none d-md-block`) que adaptan el diseño a móviles, tablets y escritorio automáticamente.
    *   Menús de navegación colapsables para móviles.

---

## 5. Proceso de Testeo (Unit Testing)

**Tu argumento:**
"Implementamos una estrategia de pruebas unitarias robusta que cubre tanto el Frontend Web como la Aplicación Móvil."

**Evidencia en tu código (¡Esto es lo que acabamos de arreglar!):**
*   **Web Comercial:**
    *   **Herramientas:** Jasmine (Framework de pruebas) + Karma (Runner) + Webpack (Bundler).
    *   **Configuración:** Archivo `karma.conf.cjs` configurado con reportes HTML (`karma-jasmine-html-reporter`) para visualización gráfica.
    *   **Mocking:** Archivo `src/datos/datosSimulados.js` que simula el Backend para probar la lógica de interfaz sin depender del servidor real (aislamiento).
*   **App Móvil (Android):**
    *   **Herramientas:** JUnit + **MockK** (ver `build.gradle.kts` línea 140: `io.mockk:mockk`).
    *   **Uso:** `InventoryViewModelTest.kt` utiliza Mocks para simular respuestas de la API y probar la lógica de negocio de los `ViewModels`.

---

## 6. Backend Framework y Base de Datos

**Tu argumento:**
"El backend es una API RESTful construida con **Spring Boot (Kotlin)**, conectada a una base de datos **PostgreSQL**."

**Evidencia en tu código:**
*   **Framework:** Spring Boot con Kotlin (ver `build.gradle.kts` del Backend).
*   **Persistencia:** JPA / Hibernate manejando las entidades.
*   **Base de Datos:** PostgreSQL (probablemente definido en `application.properties` o variables de entorno de Railway).
*   **API Doc:** Swagger UI integrado (visible en `SecurityConfig.kt` líneas 102-110 que permiten acceso público a `/swagger-ui/**`).

---

## 7. Integración REST (CRUD)

**Tu argumento:**
"La comunicación entre Front y Back es totalmente desacoplada mediante servicios REST JSON."

**Evidencia en tu código:**
*   **Frontend (Cliente):** Archivo `src/services/api.js` (o similar) que utiliza `fetch` o `axios` para llamar a los endpoints.
*   **Backend (Controladores):** Clases como `VentasController.kt` que exponen métodos HTTP:
    *   `@GetMapping` (Lectura)
    *   `@PostMapping` (Creación)
    *   `@PutMapping` (Actualización)
    *   `@DeleteMapping` (Eliminación)
*   **Integración Externa:** Conexión con APIs de IA (Gemini) e Indicadores Económicos (Mindicador.cl).

---

## 8. Seguridad (Autenticación y Autorización)

**Tu argumento:**
"La seguridad es perimetral y basada en estándares modernos. No usamos sesiones de servidor, sino Tokens JWT (Stateless)."

**Evidencia en tu código:**
*   **Configuración:** Archivo `SecurityConfig.kt`.
*   **Mecanismo:**
    *   **Stateless:** `SessionCreationPolicy.STATELESS` (línea 90).
    *   **JWT:** Filtro `JwtAuthenticationFilter` (línea 121) que intercepta cada petición validar el token.
    *   **CORS:** Configurado para permitir peticiones seguras desde tus dominios frontend (líneas 38-83).
*   **Rutas Protegidas:**
    *   `/api/auth/**` -> Públicas (Login/Registro).
    *   `/api/admin/**` -> Solo rol Administrador.
    *   `/anyRequest()` -> Requiere autenticación.

---

### 💡 Tip final para la demo:
1.  Abre la terminal en `SIGA-WEBCOMERCIAL`.
2.  Ejecuta `npx pnpm run test`.
3.  Cuando se abra Chrome, dale clic a **"DEBUG"**.
4.  Muestra los **puntos verdes** de los tests pasando. ¡Eso impresiona mucho porque demuestra calidad de software en vivo!
