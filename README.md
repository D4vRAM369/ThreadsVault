# ThreadsVault

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2B2D42?logo=kotlin&logoColor=A97BFF)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![PBL](https://img.shields.io/badge/Methodology-PBL-0A7EA4)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Claude Code](https://img.shields.io/badge/Assistant-Claude%20Code-D97706)](https://www.anthropic.com/claude-code)
[![Codex](https://img.shields.io/badge/Assistant-Codex-111827)](https://openai.com/)

<img width="400" height="400" alt="ThreadsVault-icon" src="https://github.com/user-attachments/assets/1e871a24-469d-4d54-bb55-44b1f519ddf1" />

<p align="left">
  <a href="./README_spanish-version.md">Leer en español</a>
</p>

ThreadsVault is an Android app for saving, organizing, and keeping your favorite Threads posts easily accessible in a local vault.

## Why this app

Threads in my experience, is quite good for discovering content, news, AI or programming tools (according to my personal preferences), and other interests.
However, good posts get lost quickly in the endless stream after an auto-scroll, and I don't like the idea of having access to that content only through the app's Saved section, or having to copy them one by one to another place.

For this reason, I decided to create an Android app with a vault where I can send posts, extract images using OCR (I haven't managed to get videos working), add notes, mark favorites, and organize everything into different user-created categories.

## Features

- Save Threads links from Android Share Sheet.
- Manual link add from inside the app.
- Link preview extraction and content parsing.
- Clickable hyperlinks inside saved post content.
- Clickable #hashtags with instant filter chips.
- OCR text extraction from post images.
- Quick notes per saved item.
- Categories with emoji and color, with dynamic-contrast filter chips.
- Favorites and search.
- Export to CSV and PDF.
- Backup and restore (JSON/CSV).
- Auto-backup with SAF folder selection.
- Light, dark, and system theme.
- Built-in "How to use" tutorial.

## About the Dev

This project was created by **D4vRAM**, for and by the open-source community, with love ❤️.
This project was built through **PBL (Project-Based Learning)**, using AI as a mentor in a constant learning process, as it is with every project I create. Transforming ideas and solutions to problems into code.

<sub><em>"Not using AI to program nowadays is like being a farmer and refusing to use a tractor."</em></sub>


## Privacy

- Local-first app design.
- No ads.
- No analytics SDK.
- No Firebase.
- No crash tracking service.

## Limitations (v1.0.0)

- Video extraction is still limited compared to image/OCR support.
- OCR quality depends on the source image quality and text clarity.

## Tech stack

- Kotlin
- Jetpack Compose
- Room
- DataStore
- WorkManager
- Coil
- Jsoup
- ML Kit Text Recognition (on-device OCR)

## Build

Requirements:

- Android 8.0+ (API 26)
- Android Studio (recent stable)
- JDK 17
- Android SDK 35

Commands:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

Release artifact output:

- `app/build/outputs/apk/release/`

## Current size note

- Debug APK is much larger by design.
- Current release build with minify and resource shrinking is around 43 MiB as a universal APK.
- Store delivery via AAB/split APKs reduces per-device download size.

## Roadmap ideas

- Convert it into a program or PWA available for use on Windows and Linux in the future.
- FOSS OCR version for non-Google distributions (IzzyDroid requires it, and I would like to upload it there). It also fits with my ideology as a developer and will be beneficial for uploading the application to other open-source sites.
- Better import/export UX.
- More filtering and smart organization tools.
- Maybe upload it to Google Play Store in the future.

## Author

*Created by [D4vRAM369](https://github.com/D4vRAM369) during PBL sessions, using AI as a guide.*

~

***Not vibe-coding, just vibe and code!***