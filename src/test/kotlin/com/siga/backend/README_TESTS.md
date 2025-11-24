# Tests del Backend SIGA

## Ejecutar Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar un test específico
./gradlew test --tests "com.siga.backend.AuthTest"
./gradlew test --tests "com.siga.backend.PlanesTest"
```

## Estructura de Tests

### AuthTest
- `testRegister()` - Prueba el registro de usuarios
- `testLogin()` - Prueba el login con credenciales válidas
- `testLoginInvalidCredentials()` - Prueba login con credenciales inválidas
- `testRefreshToken()` - Prueba la renovación de tokens

### ProductosTest
- `testGetProductosRequiresAuth()` - Verifica que se requiere autenticación
- `testCreateProducto()` - Prueba la creación de productos (requiere ADMIN)
- `testGetProductos()` - Prueba listar productos
- `testCreateProductoRequiresAdmin()` - Verifica que solo ADMIN puede crear

### PlanesTest
- `testGetPlanesPublic()` - Verifica que los planes son públicos
- `testGetPlanById()` - Prueba obtener un plan por ID

## Notas

- Los tests usan la base de datos real configurada en `.env`
- Para tests aislados, se recomienda usar una base de datos de test separada
- Los tests de autenticación crean usuarios temporales en la base de datos

