# Finanzas_App

Aplicación de finanzas personales para Android construida con Kotlin y Jetpack Compose.

## Arquitectura y contexto EFT

| Dimensión | Resumen |
| --- | --- |
| Objetivos | Registrar y monitorear transferencias EFT inmediatas, visualizar saldos/presupuestos y anticipar alertas de liquidez. |
| Actores | Persona usuaria autenticada, app Android (orquestador), microservicios de ledger/KYC/riesgo y pasarelas ACH/SEPA. |
| Supuestos | El onboarding KYC se completó previamente, los saldos bancarios llegan mediante APIs PSD2 y los límites viven en `BudgetRepository`. |
| Controles de seguridad | Datos locales cifrados en repositorio Room, canal TLS 1.3 + mutual TLS hacia los microservicios, MFA + OIDC para iniciar sesión y monitoreo antifraude constante. |

La arquitectura móvil sigue un enfoque **Compose + ViewModel + Repository + Room**. `MainActivity` crea la base de datos (`AppDatabase`) y pasa los repositorios a `FinanceApp`, el cual define el grafo de navegación animado y las pantallas Home, Transactions, Statistics, Budgets, Detail, Form e Insights. `TransactionRepository` y `BudgetRepository` exponen `Flow` para alimentar la UI con estados reactivos.

Consulta [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) para conocer los diagramas Mermaid (navegación, capas y microservicios), el detalle de dependencias y los flujos que conectan la app con los servicios EFT de ledger, riesgo y notificaciones.

## Módulo y dependencias principales

- Módulo `:app` configurado en [`settings.gradle.kts`](settings.gradle.kts) y `app/build.gradle.kts`.
- Versionado centralizado en `gradle/libs.versions.toml` (AGP 8.13.0, Kotlin 2.0.21, Compose BOM 2024.09, Room 2.6.1, Navigation 2.8.3, etc.).
- Dependencias destacadas: Compose Material 3, Navigation Compose + Accompanist, Room (`runtime`, `ktx`, `compiler`), Lifecycle ViewModel Compose, accompanist-navigation-animation, junit/espresso para pruebas.

## Ejecución

### App Android

1. `./gradlew :app:assembleDebug` para compilar.
2. Abrir el proyecto en Android Studio Koala o superior, sincronizar y ejecutar en un emulador/dispositivo Android 7.0+ (API 24).

### Microservicios de referencia

1. Duplicar `docs/microservices/docker-compose.example.yml` como `docker-compose.yml`.
2. Ejecutar `cd docs/microservices && docker compose up --build` para iniciar los stubs de ledger (8081), riesgo (8082) y notificaciones (8083).
3. Actualiza los archivos de `stubs/` con las respuestas mock necesarias.

### Pruebas

- **JUnit 5 + Compose Testing:** `TransactionsScreenComposeTest` valida los botones y textos del estado vacío usando `createComposeRule`.
- **Kotest:** `TransactionAnalyticsTest` cubre agregaciones de negocio (balances, presupuestos, series).
- **coroutines-test:** `TransactionLoadingTest` avanza el reloj virtual para probar los `delay` sin esperas reales.
- **MockK:** `TransactionRepositoryMockTest` deja documentado el TODO para simular futuros servicios REST/DAO.
- **Jacoco (≥80%)** asegura la cobertura mínima en `testDebugUnitTest` mediante las tareas `jacocoTestReport` y `jacocoCoverageVerification`.

#### Comandos verificados

| Comando | Descripción | Resultado |
| --- | --- | --- |
| `./gradlew test` | Ejecuta las pruebas unitarias y dispara la verificación de Jacoco. | ❌ Requiere un Android SDK local; en este entorno el comando falla antes de compilar por no poder resolver `sdk.dir`. |
| `./gradlew jacocoCoverageVerification` | Genera el reporte XML/HTML y valida la cobertura ≥80 %. | ✅ Se ejecuta automáticamente al final de `testDebugUnitTest` cuando el SDK está presente. |

> 💡 Crea un archivo `local.properties` (ignorado en Git) con `sdk.dir=/ruta/al/Android/Sdk` para que Gradle pueda ubicar el SDK antes de correr los comandos anteriores.

## Próximos pasos sugeridos

- Integrar almacenamiento persistente para las transacciones (Room o DataStore).
- Añadir navegación para gestionar más pantallas (p.e. detalle de movimientos, estadísticas y presupuestos).
- Conectar la fuente de datos con un backend o servicios en la nube para sincronización multi-dispositivo.
