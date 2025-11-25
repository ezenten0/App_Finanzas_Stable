# Paquete de entrega y firma

Este directorio contiene los artefactos de distribución y los recursos necesarios para firmar la aplicación.

## Variables de entorno requeridas
Las credenciales del keystore se consumen desde variables de entorno (útil para CI/CD). Puedes usar `release/signing.env.example`
como referencia:

- `SIGNING_KEYSTORE_PATH`: ruta al keystore (por defecto `release/finanzas-release.jks`).
- `SIGNING_KEY_ALIAS`: alias usado para la clave (`finanzasReleaseKey`).
- `SIGNING_STORE_PASSWORD`: contraseña del keystore.
- `SIGNING_KEY_PASSWORD`: contraseña de la clave (por defecto la misma que `SIGNING_STORE_PASSWORD`).

Ejemplo para cargar variables localmente sin exponerlas en el repositorio:

```bash
set -a
source release/signing.env
set +a
```

## Creación del keystore (no se versiona)
El keystore **no se almacena en el repositorio** para evitar problemas con binarios y mantener las credenciales seguras. Genera
el archivo localmente y añádelo a tu `.gitignore` (ya está ignorado por defecto):

```bash
keytool -genkeypair -v \
  -keystore release/finanzas-release.jks \
  -storetype JKS \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias finanzasReleaseKey
```

## Construcción de la versión release

1. Asegúrate de tener el keystore en `release/finanzas-release.jks` (o la ruta configurada en `SIGNING_KEYSTORE_PATH`).
2. Exporta las variables de entorno anteriores (usa secretos en tu CI/CD).
3. Ejecuta el build firmado: `./gradlew assembleRelease`.
4. El APK se genera en `app/build/outputs/apk/release/` y puedes copiarlo aquí como `app-release.apk` si necesitas compartirlo.

## Rotación de credenciales
Si necesitas renovar las credenciales, genera un nuevo keystore y actualiza las variables de entorno. Evita guardar contraseñas
en el control de versiones; mantenlas en un gestor seguro o en los secretos de tu plataforma CI.
