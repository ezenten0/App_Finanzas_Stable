# Finanzas_App

Aplicación de finanzas personales para Android construida con Kotlin y Jetpack Compose.

## Características

- Pantalla de bienvenida moderna con CTA para iniciar la experiencia.
- Pantalla principal con resumen del balance disponible, total de ingresos y gastos recientes.
- Listado de movimientos recientes con formato de moneda localizado y diferenciación visual entre ingresos y gastos.
- Arquitectura basada en `ViewModel` para exponer el estado de la interfaz de usuario de forma reactiva.
- Tema visual personalizado con nueva paleta de colores, tipografías y compatibilidad con modo claro/oscuro.

## Requisitos

- Android Studio Koala o superior.
- Gradle 8.13 y Kotlin 2.0.21 (configurados mediante el catálogo de versiones incluido).

## Ejecución

1. Clona el repositorio y ábrelo en Android Studio.
2. Sincroniza el proyecto y ejecuta la app en un emulador o dispositivo físico con Android 7.0 (API 24) o superior.

## Próximos pasos sugeridos

- Integrar almacenamiento persistente para las transacciones (Room o DataStore).
- Añadir navegación para gestionar más pantallas (p.e. detalle de movimientos, estadísticas y presupuestos).
- Conectar la fuente de datos con un backend o servicios en la nube para sincronización multi-dispositivo.
