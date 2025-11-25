# Microservicios Spring Boot (ledger, risk, notifications)

Se añadieron tres servicios con Spring Boot 3.3, capas controller/service/repository y migraciones Flyway.
Cada servicio expone perfiles `dev` (H2 en memoria) y `prod` (PostgreSQL configurable por variables de entorno).
La documentación OpenAPI se publica en `/swagger-ui.html` gracias a `springdoc-openapi-starter-webmvc-ui`.

## Ledger Service
- **Ruta base:** `/api/transactions`
- **Entidad:** `Transaction` (id, title, description, amount, type, category, eventDate, status, createdAt).
- **DTOs:** `TransactionRequest` exige `title`, `description`, `amount`, `category`, `date` y `type` (`CREDIT`/`DEBIT`); `TransactionResponse` devuelve los mismos campos más metadatos de creación.
- **Migraciones Flyway:**
  - `db/migration/V1__init.sql` crea la tabla `transactions`.
  - `db/migration/V2__add_mobile_columns.sql` añade columnas utilizadas por la app móvil (`title`, `category`, `event_date`).
- **Endpoints CRUD:**
  - `GET /api/transactions` lista todas las transacciones.
  - `GET /api/transactions/{id}` consulta por identificador.
  - `POST /api/transactions` crea con estado `POSTED`.
  - `PUT /api/transactions/{id}` actualiza contenido (tipo, montos, textos y fecha).
  - `DELETE /api/transactions/{id}` elimina si existe.

## Risk Service
- **Ruta base:** `/api/risk-cases`
- **Entidad:** `RiskCase` (id, userId, score, status, reason, createdAt).
- **DTOs:** `RiskCaseRequest` valida rango del `score` (0-100) y textos; `RiskCaseResponse` devuelve el registro.
- **Migración Flyway:** `db/migration/V1__init.sql` crea la tabla `risk_cases`.
- **Endpoints CRUD:** `GET` lista/detalle, `POST` crea, `PUT` actualiza, `DELETE` elimina.

## Notification Service
- **Ruta base:** `/api/notifications`
- **Entidad:** `Notification` (id, recipient, channel, subject, body, status, createdAt).
- **DTOs:** `NotificationRequest` valida longitudes y canal; `NotificationResponse` retorna la notificación guardada.
- **Migración Flyway:** `db/migration/V1__init.sql` crea la tabla `notifications`.
- **Endpoints CRUD:** `GET` lista/detalle, `POST` crea, `PUT` actualiza, `DELETE` elimina.

## Perfiles y ejecución
- **Dev:** H2 en memoria, `spring.jpa.hibernate.ddl-auto=validate`, Flyway habilitado.
- **Prod:** URLs y credenciales PostgreSQL se leen de variables `LEDGER_DB_URL`, `RISK_DB_URL`, `NOTIFICATION_DB_URL`, etc.
- **Levantar servicios:**
  ```bash
  ./gradlew :services:ledger-service:bootRun
  ./gradlew :services:risk-service:bootRun
  ./gradlew :services:notification-service:bootRun
  ```
- **Pruebas básicas:**
  ```bash
  ./gradlew :services:ledger-service:test
  ./gradlew :services:risk-service:test
  ./gradlew :services:notification-service:test
  ```
  
## Scripts Transactions:
```{
"title": "Sueldo mensual",
"type": "CREDIT",
"amount": 850000,
"description": "Depósito de sueldo correspondiente al mes",
"category": "Salario",
"date": "2025-11-25"
}
{
"title": "Supermercado",
"type": "DEBIT",
"amount": 65432.5,
"description": "Compra de alimentos y artículos de limpieza",
"category": "Alimentación",
"date": "2025-11-24"
}
{
"title": "Suscripción Netflix",
"type": "DEBIT",
"amount": 8990,
"description": "Pago mensual de servicio de streaming",
"category": "Entretenimiento",
"date": "2025-11-20"
}
{
"title": "Venta teclado mecánico",
"type": "CREDIT",
"amount": 45000,
"description": "Ingreso por venta de teclado usado",
"category": "Ingresos extra",
"date": "2025-11-18"
}
{
"title": "Cuenta de luz",
"type": "DEBIT",
"amount": 32780,
"description": "Pago de boleta de electricidad domiciliaria",
"category": "Servicios básicos",
"date": "2025-11-15"
}
{
"title": "Cuenta de luz",
"type": "DEBIT",
"amount": 32780,
"description": "Pago de boleta de electricidad domiciliaria",
"category": "Servicios básicos",
"date": "2025-11-15"
}
{
"title": "Transporte mensual",
"type": "DEBIT",
"amount": 42000,
"description": "Gasto en transporte público y aplicaciones de viaje",
"category": "Transporte",
"date": "2025-11-05"
}
{
"title": "Clases particulares",
"type": "CREDIT",
"amount": 60000,
"description": "Ingreso por tutorías de programación",
"category": "Servicios profesionales",
"date": "2025-11-02"
}```
