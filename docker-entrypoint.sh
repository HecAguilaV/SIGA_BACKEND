#!/bin/sh
set -e

# Logging inmediato
echo "========================================"
echo "DOCKER ENTRYPOINT INICIADO"
echo "========================================"
echo "Working directory: $(pwd)"
echo "Files in directory:"
ls -la

# Verificar que el JAR existe
if [ ! -f "app.jar" ]; then
    echo "ERROR: app.jar no encontrado!"
    exit 1
fi

echo "JAR encontrado: $(ls -lh app.jar)"

# Obtener el puerto
PORT=${PORT:-8080}
echo "Puerto: $PORT"
echo "JAVA_OPTS: ${JAVA_OPTS}"

# Verificar Java
echo "Java version:"
java -version 2>&1

echo "========================================"
echo "INICIANDO APLICACIÓN"
echo "========================================"

# Ejecutar la aplicación
exec java ${JAVA_OPTS} -jar app.jar --server.port=$PORT --server.address=0.0.0.0

