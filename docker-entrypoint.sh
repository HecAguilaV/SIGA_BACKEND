#!/bin/sh
set -e

echo "========================================"
echo "DOCKER ENTRYPOINT INICIADO"
echo "========================================"

# Verificar que el JAR existe
if [ ! -f "app.jar" ]; then
    echo "ERROR: app.jar no encontrado!"
    ls -la
    exit 1
fi

echo "JAR encontrado: $(ls -lh app.jar)"

# Obtener el puerto
PORT=${PORT:-8080}
echo "Puerto configurado: $PORT"
echo "JAVA_OPTS: ${JAVA_OPTS:--Xmx512m -Xms256m}"

# Verificar Java
echo "Java version:"
java -version

echo "========================================"
echo "INICIANDO APLICACIÓN SPRING BOOT"
echo "========================================"

# Ejecutar la aplicación con logging detallado
exec java ${JAVA_OPTS:--Xmx512m -Xms256m} \
    -Dspring.profiles.active=prod \
    -Dlogging.level.root=INFO \
    -Dlogging.level.com.siga.backend=DEBUG \
    -jar app.jar \
    --server.port=$PORT \
    --server.address=0.0.0.0

