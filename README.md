# Finanzas 3.0 — App Android + Firestore + Microservicios (Spring Boot)

Aplicación de finanzas personales para Android (Kotlin + Jetpack Compose) con arquitectura **MVVM**, persistencia local con **Room** y sincronización en la nube con **Firebase Auth + Cloud Firestore**.  
El repositorio también incluye microservicios **Spring Boot** (ledger/risk/notification) y un modo de ejecución con **stubs (WireMock)** para demos rápidas.

---

## ✅ Funcionalidades principales (App móvil)

### Autenticación
- Login con **Firebase Auth**
  - Correo/contraseña
  - **Google Sign-In** (Play Services Auth)

### Movimientos (Transactions)
- CRUD de movimientos (ingreso/gasto)
- Sincronización:
  - **Local-first** (Room)
  - Sincronización cloud (Firestore) y/o REST (microservicios) según configuración
- **Actualizaciones en tiempo real** (SSE) cuando se usa backend

### Presupuestos (Budgets)
- CRUD de presupuestos por categoría
- Progreso y alertas visuales (ej: cuando se aproxima o supera el límite)

### Insights / Estadísticas
- Agregaciones mensuales en Firestore (totales, por categoría)
- Integración con servicio de riesgo (risk-service) para insights enriquecidos

### UX/UI (Material Design 3)
- UI reactiva con **State/StateFlow**
- Animaciones funcionales (AnimatedVisibility / AnimatedContent / animate*AsState)
- Navegación con Navigation Compose + animaciones

---

## 🧱 Arquitectura (MVVM)

- **Screens / Composables (UI)**: renderizan el estado y emiten eventos (intentos del usuario).
- **ViewModel**: orquesta estado (`StateFlow`/`State`), validación y llamadas a repositorios.
- **Repository**: fuente unificada de datos (Room + Firestore + Retrofit).
- **Model/DTO/Entity**:
  - Model (dominio UI)
  - Entity (Room)
  - DTO (HTTP)

---

## 🗂️ Estructura del código fuente (App y Microservicios)

### App Android
- Módulo: `:app`
- Código: `app/src/main/java/com/example/app_finanzas/`
- Capas clave:
  - `data/local/` → Room (DB/DAO/Entities)
  - `data/cloud/` → Firestore repositories
  - `data/remote/` + `network/` → Retrofit/OkHttp/SSE
  - `home/`, `transactions/`, `budgets/`, `insights/`, `statistics/`, `auth/` → features/UI/ViewModels

### Microservicios (Spring Boot)
- `services/ledger-service` → movimientos (transactions) + SSE
- `services/risk-service` → insights + budget alerts + risk cases
- `services/notification-service` → notificaciones + SSE

---

## 🔥 Firebase / Firestore (uso real en la app)

### Config Firebase
- Archivo requerido: `app/google-services.json`
- Dependencias (Firebase BoM + Auth + Firestore) ya incluidas en `app/build.gradle.kts`.

### Reglas Firestore
- Reglas del repo: `firestore.rules`
- Enfoque: cada usuario solo puede leer/escribir bajo su documento:
  - `users/{userId}/**` solo accesible si `request.auth.uid == userId`

### Estructura de datos (Firestore)
Colección por usuario:

- `users/{uid}/transactions/{transactionId}`
- `users/{uid}/budgets/{budgetId}`
- Agregados mensuales (insights):
  - `users/{uid}/insights/monthly/monthly/{monthKey}`

> La app mantiene agregados mensuales en Firestore al insertar/actualizar/eliminar transacciones (para que Insights sea rápido y “cloud-friendly”).

---

## 🌐 Endpoints usados (propios y externos)

### ✅ Microservicios propios (Spring Boot)

> **Modo microservicios reales (puertos por defecto del backend en este repo):**
- Ledger: `http://localhost:8080`
- Risk: `http://localhost:8081`
- Notification: `http://localhost:8082`

> **Swagger/OpenAPI (por servicio):**
- `http://localhost:<PUERTO>/swagger-ui/index.html`
- `http://localhost:<PUERTO>/v3/api-docs`

#### 1) Ledger Service — Transactions
Base: `/api/transactions`

- `GET    /api/transactions` → listar
- `GET    /api/transactions/{id}` → obtener por id
- `POST   /api/transactions` → crear
- `PUT    /api/transactions/{id}` → actualizar
- `DELETE /api/transactions/{id}` → eliminar
- `GET    /api/transactions/stream` → **SSE** (text/event-stream)

#### 2) Risk Service — Insights + Budget Alerts + Risk Cases
**Insights**
- `GET  /api/v1/insights` → insights por defecto
- `POST /api/v1/insights` → insights con contexto (presupuestos/progreso)

**Budget Alerts**
- `POST /api/v1/budget-alerts` → webhook/alertas desde móvil

**Risk Cases**
Base: `/api/risk-cases`
- `GET    /api/risk-cases`
- `GET    /api/risk-cases/{id}`
- `POST   /api/risk-cases`
- `PUT    /api/risk-cases/{id}`
- `DELETE /api/risk-cases/{id}`

#### 3) Notification Service — Notifications
Base: `/api/notifications`
- `GET    /api/notifications`
- `GET    /api/notifications/{id}`
- `POST   /api/notifications`
- `PUT    /api/notifications/{id}`
- `DELETE /api/notifications/{id}`
- `GET    /api/notifications/stream` → **SSE**

---

### 🌍 API externa (pública)
- Risk-service consume tasas FX desde:
  - `https://api.exchangerate.host/latest?base=USD&symbols=MXN,EUR`
- Configurable por property:
  - `external.fx.url=...`

---

### ☁️ Servicios externos (no REST propio)
- **Firebase Auth** (login email/password + Google)
- **Cloud Firestore** (lectura/escritura por SDK, no por endpoints REST propios)

---

## ▶️ Instrucciones para ejecutar el proyecto

### Requisitos
- Android Studio (Koala o superior recomendado)
- JDK 17 (para microservicios Spring Boot)
- Android SDK instalado (`sdk.dir` en `local.properties`)
- (Opcional) Docker + Docker Compose (para modo stubs)

---

### 1) Ejecutar App Android (debug)
1. Abrir el proyecto en Android Studio.
2. Verificar `local.properties` (en la raíz) con `sdk.dir=...`.
3. Ejecutar en emulador/dispositivo:
   - `./gradlew :app:assembleDebug`
   - o Run desde Android Studio

---

### 2) Configurar URLs base (conexión a backend)
La app lee URLs desde **`local.properties`** (raíz) usando:
- `LEDGER_BASE_URL`
- `RISK_BASE_URL`
- `NOTIF_BASE_URL`

📌 Ejemplo recomendado si corres microservicios reales (emulador Android):
```properties
LEDGER_BASE_URL=http://10.0.2.2:8080
RISK_BASE_URL=http://10.0.2.2:8081
NOTIF_BASE_URL=http://10.0.2.2:8082
