# ThreadsVault
<img width="400" height="400" alt="ThreadsVault-icon" src="https://github.com/user-attachments/assets/1e871a24-469d-4d54-bb55-44b1f519ddf1" />

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2B2D42?logo=kotlin&logoColor=A97BFF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![PBL](https://img.shields.io/badge/Methodology-PBL-0A7EA4)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Claude Code](https://img.shields.io/badge/Assistant-Claude%20Code-D97706)](https://www.anthropic.com/claude-code)
[![Codex](https://img.shields.io/badge/Assistant-Codex-111827)](https://openai.com/)


[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2B2D42?logo=kotlin&logoColor=A97BFF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![PBL](https://img.shields.io/badge/Methodology-PBL-0A7EA4)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Claude Code](https://img.shields.io/badge/Assistant-Claude%20Code-D97706)](https://www.anthropic.com/claude-code)
[![Codex](https://img.shields.io/badge/Assistant-Codex-111827)](https://openai.com/)

<p align="left">
  <a href="./README.md">Read in English</a>
</p>

ThreadsVault es una app Android para guardar, organizar y tener a mano tus posts favoritos de Threads en una bóveda local.

## Por qué existe

Threads en mi experiencia es bastante bueno para descubrir contenido, noticias, herramientas de IA o de programación (en cuanto a mi preferencia personal) y demás intereses.
Pero los buenos posts se pierden rápido en la infinidad tras un autoscroll, y no me gusta la idea de únicamente tener acceso a ese contenido en Guardados dentro de la app, o tener que copiar uno a uno para llevarlos a otro sitio. Por ello he decidido crear una app para Android con una bóveda donde poder enviar las publicaciones y con extracción de imágenes mediante OCR (vídeos no he conseguido hacerlos funcionar), con posibilidad de añadir notas, favoritos y dividirlo en distintas categorías que se pueden crear.


## Funciones

- Guardar enlaces de Threads desde el Share Sheet de Android.
- Agregado manual de enlaces dentro de la app.
- Extracción de preview del enlace y parsing de contenido.
- OCR de texto desde imágenes del post.
- Notas rápidas por elemento guardado.
- Categorías con emoji opcional.
- Favoritos y buscador.
- Exportar a CSV y PDF.
- Backup y restore (JSON/CSV).
- Autobackup con selección de carpeta SAF.
- Tema claro, oscuro o sistema.

## Privacidad

- Diseño local-first.
- Sin anuncios.
- Sin SDK de analítica.
- Sin Firebase.
- Sin servicio de crash tracking.

## Stack técnico

- Kotlin
- Jetpack Compose
- Room
- DataStore
- WorkManager
- Coil
- Jsoup
- ML Kit Text Recognition (OCR on-device)

## Build

Requisitos:

- Android Studio (stable reciente)
- JDK 17
- Android SDK 35

Comandos:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

## Nota de tamaño actual

- El APK debug pesa bastante mas por diseño.
- El release actual con minify + shrinkResources ronda 43 MiB como APK universal.
- Con AAB/splits, el tamaño final por dispositivo baja bastante.

## Ideas de roadmap

- Convertirlo en programa o PWA disponible para usar en Windows y Linux más adelante.
- Flavor FOSS de OCR para distribuciones sin Google.
- Mejor UX de import/export.
- Mas filtros y organización inteligente.
- Nuevas funciones que irán surgiendo sobre la marcha en cualquier momento, y en mis sesiones de estudio PBL de horas interminables con diferentes IAs y herramientas.

## Autor

Creado por [D4vRAM369](https://github.com/D4vRAM369)
