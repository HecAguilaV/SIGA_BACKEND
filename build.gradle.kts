import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.siga"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux") // Para HTTP client (Gemini API)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")
    
    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    
    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Logging (Spring Boot incluye Logback, pero especificamos versión compatible)
    // Logback se incluye automáticamente con Spring Boot, no necesitamos especificarlo
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Optimizaciones para builds más rápidos
tasks.withType<JavaCompile> {
    options.isIncremental = true
}

// Cacheo de dependencias
configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
        cacheDynamicVersionsFor(0, "seconds")
    }
}

// Spring Boot no necesita mainClass, lo detecta automáticamente

// Tarea para ejecutar migraciones
tasks.register<JavaExec>("migrate") {
    group = "database"
    description = "Ejecuta las migraciones de base de datos"
    mainClass.set("com.siga.backend.MigrateDatabaseKt")
    classpath = sourceSets["main"].runtimeClasspath
}

// Tarea para verificar tablas
tasks.register<JavaExec>("verifyTables") {
    group = "database"
    description = "Verifica que las tablas se crearon correctamente"
    mainClass.set("com.siga.backend.database.VerifyTablesKt")
    classpath = sourceSets["main"].runtimeClasspath
}

// Tarea para verificar base de datos
tasks.register<JavaExec>("verifyDb") {
    group = "database"
    description = "Verifica la conexion a la base de datos"
    mainClass.set("com.siga.backend.VerifyDatabaseKt")
    classpath = sourceSets["main"].runtimeClasspath
}