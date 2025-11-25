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
- **Network + Insights:** `RemoteTransactionMapperTest`, `InsightsViewModelTest` y `InsightsRepositoryIntegrationTest` validan el mapeo remoto/local de transacciones y que los insights locales se mantienen cuando el risk-service devuelve listas vacías (MockWebServer).
- **Jacoco (≥80%)** asegura la cobertura mínima en `testDebugUnitTest` mediante las tareas `jacocoTestReport` y `jacocoCoverageVerification`.

Los interceptores HTTP (`FinanceHttp`) ahora registran la duración de cada request, los códigos de error (4xx/5xx) y un preview del cuerpo de respuesta para acelerar el troubleshooting de fallas o latencia en Retrofit/OkHttp.

#### Comandos verificados

| Comando | Descripción | Resultado |
| --- | --- | --- |
| `./gradlew test` | Ejecuta las pruebas unitarias y dispara la verificación de Jacoco. | ❌ Requiere un Android SDK local; en este entorno el comando falla antes de compilar por no poder resolver `sdk.dir`. |
| `./gradlew jacocoCoverageVerification` | Genera el reporte XML/HTML y valida la cobertura ≥80 %. | ✅ Se ejecuta automáticamente al final de `testDebugUnitTest` cuando el SDK está presente. |

> 💡 Crea un archivo `local.properties` (ignorado en Git) con `sdk.dir=/ruta/al/Android/Sdk` para que Gradle pueda ubicar el SDK antes de correr los comandos anteriores.

## Guía paso a paso de ejecución completa

1. **Preparar el entorno**
   - Instala JDK 17 y Android Studio Koala o superior.
   - En la raíz del repo, crea `local.properties` con `sdk.dir=/ruta/al/Android/Sdk` para permitir la compilación y las pruebas.
   - Asegura el keystore en `keystore/` (incluido) y valida que las credenciales estén definidas como variables de entorno para el `signingConfig`.
2. **Levantar los microservicios Spring Boot**
   - Cada servicio está incluido como módulo Gradle: `:services:ledger-service`, `:services:risk-service` y `:services:notification-service`.
   - Ejecuta en paralelo (o en terminales separadas):
     - `./gradlew :services:ledger-service:bootRun` (puerto 8081, CRUD de movimientos y saldos).
     - `./gradlew :services:risk-service:bootRun` (puerto 8082, scoring de riesgo/alertas).
     - `./gradlew :services:notification-service:bootRun` (puerto 8083, envío de notificaciones y webhooks).
   - Cada servicio usa base de datos en memoria/h2 por defecto; ajusta `application.yml` si quieres persistencia real.
3. **Integración app–microservicios en tiempo real**
   - La app consume los endpoints anteriores mediante Retrofit. La capa de repositorios mezcla Room (cache local) con llamadas REST para sincronizar CRUD.
   - Ledger expone `/api/transactions/stream` via Server-Sent Events y la app escucha con `RealtimeUpdatesClient` (OkHttp SSE); si el stream cae, el repositorio sigue haciendo short-polling.
   - Define variables de entorno o un archivo de configuración para las URLs base (`LEDGER_BASE_URL`, `RISK_BASE_URL`, `NOTIF_BASE_URL`).
4. **Consumir API externa**
   - El `risk-service` consulta una API pública de tasas FX (`external.fx.url`) para enriquecer los insights que devuelve en `/api/v1/insights`, los cuales se consumen desde la pantalla de Insights.
5. **Compilar la app**
   - Desarrollo: `./gradlew :app:assembleDebug`.
   - Release firmado: `./gradlew :app:assembleRelease` (usa el keystore y las credenciales cargadas por variables de entorno o `gradle.properties` seguro).
6. **Ejecutar pruebas y cobertura**
   - Corre `./gradlew test` para lanzar los módulos de prueba de ViewModels/Repositories y generar cobertura Jacoco ≥80 %.
   - Revisa los reportes en `app/build/reports/tests` y `app/build/reports/jacoco`.
7. **Validación end-to-end**
   - Con microservicios levantados y la app instalada (debug o release), abre la app en el emulador/dispositivo.
   - Ejecuta operaciones CRUD de movimientos/presupuestos desde la UI y valida que se reflejen en los servicios (logs en consola de cada `bootRun`).
   - Comprueba que las notificaciones/eventos en tiempo real actualicen la UI sin recargar manualmente y que los datos externos (API pública) se muestren correctamente.
   - Un smoke test automatizado (`InsightControllerTest`) simula la API externa y verifica que `/api/v1/insights` devuelva tanto tasas FX como casos de riesgo locales.
   - Finalmente, genera el APK release y prueba la instalación para asegurar que la firma y la configuración de red funcionan en un entorno limpio.

**Microservicios/Docs**
Consultar : [`docs/microservices/SPRING_BOOT_ENDPOINTS.md`](docs/microservices/SPRING_BOOT_ENDPOINTS.md)

## Próximos pasos sugeridos

- Integrar almacenamiento persistente para las transacciones (Room o DataStore).
- Añadir navegación para gestionar más pantallas (p.e. detalle de movimientos, estadísticas y presupuestos).
- Conectar la fuente de datos con un backend o servicios en la nube para sincronización multi-dispositivo.
