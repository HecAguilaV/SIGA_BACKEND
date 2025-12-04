#!/bin/sh
set -e

# Obtener el puerto de la variable de entorno PORT, o usar 8080 por defecto
PORT=${PORT:-8080}

echo "========================================"
echo "Iniciando SIGA Backend"
echo "Puerto: $PORT"
echo "JAVA_OPTS: ${JAVA_OPTS:--Xmx512m -Xms256m}"
echo "========================================"

# Ejecutar la aplicación Spring Boot
exec java ${JAVA_OPTS:--Xmx512m -Xms256m} -jar app.jar --server.port=$PORT --server.address=0.0.0.0

