# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./gradlew build -x test --no-daemon

# Find and copy the executable JAR (exclude -plain.jar)
RUN find /app/build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} /app/app.jar \;

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the executable JAR from build stage
COPY --from=build /app/app.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
# Railway asigna PORT dinámicamente, leer de variable de entorno
# Usar exec para que Java reciba señales correctamente
# Forzar server.address=0.0.0.0 para aceptar conexiones externas
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080} --server.address=0.0.0.0"]

