# Configuración Railway

## Pasos para Desplegar

### 1. Conectar Repositorio

1. Ir a [Railway](https://railway.app)
2. Crear nuevo proyecto
3. Conectar repositorio GitHub: `HecAguilaV/SIGA_BACKEND`
4. Railway detectará automáticamente el proyecto Kotlin

### 2. Configurar Variables de Entorno

En Railway, agregar las siguientes variables de entorno:

```
DATABASE_URL=jdbc:postgresql://postgresql-hector.alwaysdata.net:5432/hector_siga_db
DB_USER=hector
DB_PASSWORD=kike4466
GEMINI_API_KEY=AIzaSyCFP_toj6X_q7ye_1Sbt8W1gKAC1tMgKdQ
JWT_SECRET=tu_secret_key_super_seguro_cambiar_en_produccion
PORT=8080
ALLOWED_ORIGINS=https://siga-appweb.vercel.app,https://siga-web.vercel.app,http://localhost:5173,http://localhost:3000
```

**IMPORTANTE**: 
- Cambiar `JWT_SECRET` por uno seguro en producción
- Rotar `GEMINI_API_KEY` si fue expuesta anteriormente
- `PORT` lo asigna Railway automáticamente, pero se puede configurar
- Actualizar `ALLOWED_ORIGINS` con las URLs reales de tus frontends en Vercel

### 3. Configurar Build

Railway usará:
- Builder: NIXPACKS (detecta automáticamente Gradle)
- Start Command: `./gradlew run`
- Health Check: `/health`

### 4. Verificar Despliegue

1. Railway construirá el proyecto automáticamente
2. Verificar logs en Railway dashboard
3. Probar endpoint: `https://tu-proyecto.railway.app/health`
4. Debe retornar: `{"status":"healthy","database":"connected",...}`

### 5. Obtener URL de Producción

Railway asignará una URL como:
- `https://siga-backend-production.up.railway.app`

Actualizar `ALLOWED_ORIGINS` con las URLs de los frontends en producción.

## Troubleshooting

### Error: "No se puede conectar a la base de datos"
- Verificar que `DATABASE_URL`, `DB_USER`, `DB_PASSWORD` estén correctos
- Verificar que Always Data permita conexiones desde Railway

### Error: "Port already in use"
- Railway asigna el puerto automáticamente
- Verificar que `PORT` esté configurado o dejar que Railway lo asigne

### Error: "Build failed"
- Revisar logs en Railway
- Verificar que `build.gradle.kts` esté correcto
- Verificar que todas las dependencias estén disponibles

## Notas

- Railway tiene un plan gratuito con límites
- Considerar usar Railway PostgreSQL en el futuro para mejor integración
- Los logs están disponibles en el dashboard de Railway

