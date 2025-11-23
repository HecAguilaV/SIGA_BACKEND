# 🛠️ Guía de Configuración: Backend SIGA en IntelliJ IDEA

Esta guía te ayudará a configurar el proyecto backend de SIGA usando IntelliJ IDEA.

---

## 📋 Requisitos Previos

- **IntelliJ IDEA** (Community o Ultimate)
- **JDK 21** o superior
- **PostgreSQL** (local para desarrollo, o credenciales de Always Data)
- **Git** instalado
- **Cuenta en Railway** (para despliegue futuro)

---

## 🚀 Paso 1: Crear el Proyecto

### Opción A: Desde IntelliJ IDEA

1. **Abrir IntelliJ IDEA**
2. **File → New → Project**
3. **Seleccionar**: Kotlin → Gradle → Kotlin DSL
4. **Configurar**:
   - **Name**: `SIGA_Backend`
   - **Location**: `/Users/hector/Desktop/Encargo2APP/SIGA_Backend`
   - **JDK**: 21
   - **Build system**: Gradle
   - **DSL**: Kotlin
5. **Click**: Create

### Opción B: Desde Terminal (Recomendado)

```bash
cd /Users/hector/Desktop/Encargo2APP
mkdir SIGA_Backend
cd SIGA_Backend

# Inicializar proyecto Gradle
gradle init \
  --type kotlin-application \
  --dsl kotlin \
  --package com.siga.backend \
  --project-name SIGA_Backend \
  --test-framework kotlin-test-junit5
```

Luego abre el proyecto en IntelliJ IDEA:
- **File → Open** → Selecciona la carpeta `SIGA_Backend`

---

## 📦 Paso 2: Configurar build.gradle.kts

Reemplaza el contenido de `build.gradle.kts` con:

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.siga"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-cors:2.3.5")
    implementation("io.ktor:ktor-server-auth:2.3.5")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.5")
    
    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")
    
    // Exposed (ORM)
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.44.1")
    
    // Serialización
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Google Generative AI (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // HikariCP (Connection Pool)
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

application {
    mainClass.set("com.siga.backend.ApplicationKt")
}
```

---

## 📁 Paso 3: Estructura de Carpetas

Crea la siguiente estructura en `src/main/kotlin/com/siga/backend/`:

```
src/main/kotlin/com/siga/backend/
├── Application.kt              # Punto de entrada
├── config/
│   ├── DatabaseConfig.kt       # Configuración PostgreSQL
│   └── ApplicationConfig.kt    # Configuración general
├── api/
│   ├── comercial/
│   │   └── ChatRoutes.kt       # Endpoint asistente comercial
│   ├── saas/
│   │   └── ChatRoutes.kt       # Endpoint asistente operativo
│   ├── productos/
│   │   └── ProductosRoutes.kt
│   └── auth/
│       └── AuthRoutes.kt
├── database/
│   ├── Database.kt             # Conexión a BD
│   └── schemas/
│       ├── SigaSaasSchema.kt   # Esquema siga_saas
│       └── SigaComercialSchema.kt # Esquema siga_comercial
├── models/
│   ├── Producto.kt
│   ├── Local.kt
│   └── Usuario.kt
├── services/
│   ├── CommercialAssistantService.kt
│   ├── OperationalAssistantService.kt
│   └── GeminiService.kt
└── utils/
    └── RAGContextBuilder.kt
```

**Para crear las carpetas en IntelliJ**:
1. Click derecho en `com/siga/backend`
2. **New → Package**
3. Crea cada paquete (carpeta)

---

## ⚙️ Paso 4: Configurar Variables de Entorno

### Crear archivo `.env` (para desarrollo local)

En la raíz del proyecto, crea `.env`:

```env
# Base de Datos
DATABASE_URL=jdbc:postgresql://localhost:5432/siga_db
DB_USER=postgres
DB_PASSWORD=tu_password_local

# API Keys
GEMINI_API_KEY=AIzaSyCFP_toj6X_q7ye_1Sbt8W1gKAC1tMgKdQ

# JWT
JWT_SECRET=tu_secret_key_super_seguro_aqui

# Server
PORT=8080
```

### Agregar al .gitignore

```gitignore
.env
.idea/
build/
*.iml
```

---

## 🔧 Paso 5: Configurar IntelliJ IDEA

### 5.1 Configurar JDK

1. **File → Project Structure** (⌘; en Mac, Ctrl+Alt+Shift+S en Windows)
2. **Project Settings → Project**
3. **SDK**: Selecciona JDK 21
4. **Language level**: 21

### 5.2 Configurar Gradle

1. **File → Settings** (⌘, en Mac, Ctrl+Alt+S en Windows)
2. **Build, Execution, Deployment → Build Tools → Gradle**
3. **Build and run using**: IntelliJ IDEA (o Gradle)
4. **Run tests using**: IntelliJ IDEA

### 5.3 Instalar Plugins Recomendados

1. **File → Settings → Plugins**
2. Instala:
   - **Kotlin** (ya viene)
   - **Gradle** (ya viene)
   - **Database Navigator** (opcional, para ver BD)

---

## 🗄️ Paso 6: Configurar Conexión a PostgreSQL

### Opción A: PostgreSQL Local (Desarrollo)

1. Instala PostgreSQL localmente
2. Crea base de datos:
```sql
CREATE DATABASE siga_db;
```

### Opción B: Always Data (Producción/Desarrollo)

1. Obtén credenciales de Always Data
2. Actualiza `.env` con las credenciales reales

---

## 🚀 Paso 7: Crear Archivo Principal

Crea `src/main/kotlin/com/siga/backend/Application.kt`:

```kotlin
package com.siga.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.siga.backend.config.*
import com.siga.backend.api.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configurar CORS
    configureCORS()
    
    // Configurar serialización JSON
    configureSerialization()
    
    // Configurar base de datos
    configureDatabase()
    
    // Configurar rutas
    configureRoutes()
}

fun Application.configureRoutes() {
    // Rutas de asistentes
    comercialChatRoutes()
    saasChatRoutes()
    
    // Otras rutas
    // productosRoutes()
    // authRoutes()
}
```

---

## ✅ Paso 8: Verificar Configuración

1. **Sincronizar Gradle**:
   - Click en el icono de Gradle en la barra lateral
   - O: **File → Sync Project with Gradle Files**

2. **Verificar dependencias**:
   - Deberían descargarse automáticamente
   - Revisa la pestaña "Build" en la parte inferior

3. **Ejecutar proyecto**:
   - Click derecho en `Application.kt`
   - **Run 'ApplicationKt'**
   - O presiona `Shift + F10`

4. **Verificar que funciona**:
   - Abre navegador: `http://localhost:8080`
   - Deberías ver respuesta (o error 404 si no hay rutas aún)

---

## 🐛 Solución de Problemas Comunes

### Error: "JDK not found"
- **Solución**: Configura JDK 21 en Project Structure

### Error: "Gradle sync failed"
- **Solución**: 
  1. **File → Invalidate Caches / Restart**
  2. Sincroniza nuevamente

### Error: "Cannot resolve symbol"
- **Solución**: 
  1. **File → Sync Project with Gradle Files**
  2. Espera a que descargue dependencias

### Error: "Port 8080 already in use"
- **Solución**: Cambia el puerto en `Application.kt` o mata el proceso:
```bash
lsof -ti:8080 | xargs kill
```

---

## 📚 Próximos Pasos

1. ✅ Proyecto creado y configurado
2. ⏭️ Crear configuración de base de datos
3. ⏭️ Crear esquemas (siga_saas, siga_comercial)
4. ⏭️ Implementar endpoints básicos
5. ⏭️ Integrar asistentes IA

---

## 🔗 Recursos Útiles

- [Documentación Ktor](https://ktor.io/docs/)
- [Documentación Exposed](https://github.com/JetBrains/Exposed)
- [Documentación PostgreSQL JDBC](https://jdbc.postgresql.org/documentation/)
- [IntelliJ IDEA Help](https://www.jetbrains.com/help/idea/)

---

**¿Problemas?** Revisa los logs en la consola de IntelliJ o consulta la documentación oficial.

