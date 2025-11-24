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
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-server-cors:2.3.5")
    implementation("io.ktor:ktor-server-auth:2.3.5")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.5")
    
    // Ktor Client (para llamadas a Gemini API)
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-cio:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    
    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")
    
    // Exposed (ORM)
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    
    // Serialización
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Google Generative AI (Gemini)
    // Nota: Usaremos la API REST directamente con Ktor Client
    // implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // HikariCP (Connection Pool)
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.5")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

application {
    mainClass.set("com.siga.backend.ApplicationKt")
}

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
    description = "Verifica el estado de la base de datos"
    mainClass.set("com.siga.backend.VerifyDatabaseKt")
    classpath = sourceSets["main"].runtimeClasspath
}