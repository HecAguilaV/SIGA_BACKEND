# 🧪 Testing - Backend SIGA

## Ejecutar Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar un test específico
./gradlew test --tests "com.siga.backend.AuthTest"
./gradlew test --tests "com.siga.backend.PlanesTest"
./gradlew test --tests "com.siga.backend.ProductosTest"
./gradlew test --tests "com.siga.backend.StockTest"

# Ver reporte detallado
open build/reports/tests/test/index.html
```

## Estructura de Tests

### ✅ AuthTest (4 tests)
- `testRegister()` - Prueba el registro de usuarios
- `testLogin()` - Prueba el login con credenciales válidas
- `testLoginInvalidCredentials()` - Prueba login con credenciales inválidas
- `testRefreshToken()` - Prueba la renovación de tokens

### ✅ ProductosTest (4 tests)
- `testGetProductosRequiresAuth()` - Verifica que se requiere autenticación
- `testCreateProducto()` - Prueba la creación de productos (requiere ADMIN)
- `testGetProductos()` - Prueba listar productos
- `testCreateProductoRequiresAdmin()` - Verifica que solo ADMIN puede crear

### ✅ PlanesTest (2 tests)
- `testGetPlanesPublic()` - Verifica que los planes son públicos (no requieren auth)
- `testGetPlanById()` - Prueba obtener un plan por ID

### ✅ StockTest (2 tests)
- `testGetStockRequiresAuth()` - Verifica que se requiere autenticación
- `testGetStock()` - Prueba listar stock

## Cobertura de Tests

### Endpoints Probados
- ✅ Autenticación (register, login, refresh)
- ✅ Productos CRUD (con validación de roles)
- ✅ Stock (listar)
- ✅ Planes (públicos)

### Endpoints Pendientes de Tests
- ⏳ Ventas (crear, listar)
- ⏳ Suscripciones (crear, listar)
- ⏳ Asistentes IA (comercial y operativo)

## Notas Importantes

1. **Base de Datos**: Los tests usan la base de datos real configurada en `.env`
   - Para producción, se recomienda usar una base de datos de test separada
   - Los tests crean usuarios temporales que pueden quedar en la BD

2. **Autenticación**: Los tests verifican:
   - Que los endpoints protegidos requieren JWT
   - Que los roles se validan correctamente (ADMIN vs OPERADOR)
   - Que los endpoints públicos funcionan sin autenticación

3. **Limpieza**: Los tests no limpian datos después de ejecutarse
   - Considerar agregar `@BeforeEach` y `@AfterEach` para limpiar datos de test

## Mejoras Futuras

1. **Base de Datos de Test**: Configurar una BD separada para tests
2. **Tests de Integración**: Agregar tests end-to-end
3. **Mocking**: Usar mocks para servicios externos (Gemini API)
4. **Cobertura**: Aumentar cobertura de código (objetivo: >80%)
5. **Performance**: Agregar tests de rendimiento

## Ejecución Continua

Los tests se ejecutan automáticamente en:
- Pre-commit hooks (recomendado)
- CI/CD pipeline (cuando se configure)

