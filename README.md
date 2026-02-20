# ThreadsVault

<p align="left">
  <a href="./README_spanish-version.md">Leer en espanol</a>
</p>

ThreadsVault is an Android app for saving, organizing, and keeping your favorite Threads posts easily accessible in a local vault.

## Why this app

Threads in my experience, is quite good for discovering content, news, AI or programming tools (according to my personal preferences), and other interests.
However, good posts get lost quickly in the endless stream after an auto-scroll, and I don’t like the idea of having access to that content only through the app’s Saved section, or having to copy them one by one to another place.

For this reason, I decided to create an Android app with a vault where I can send posts, extract images using OCR (I haven’t managed to get videos working), add notes, mark favorites, and organize everything into different user-created categories.


## Features

- Save Threads links from Android Share Sheet.
- Manual link add from inside the app.
- Link preview extraction and content parsing.
- OCR text extraction from post images.
- Quick notes per saved item.
- Categories with optional emoji.
- Favorites and search.
- Export to CSV and PDF.
- Backup and restore (JSON/CSV).
- Auto-backup with SAF folder selection.
- Light, dark, and system theme.

## Privacy

- Local-first app design.
- No ads.
- No analytics SDK.
- No Firebase.
- No crash tracking service.

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

- Android Studio (recent stable)
- JDK 17
- Android SDK 35

Commands:

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

## Current size note

- Debug APK is much larger by design.
- Current release build with minify and resource shrinking is around 43 MiB as a universal APK.
- Store delivery via AAB/split APKs reduces per-device download size.

## Roadmap ideas

- Convert it into a program or PWA available for use on Windows and Linux in the future.
- FOSS OCR flavor for non-Google distributions.
- Better import/export UX.
- More filtering and smart organization tools.

## Author

Created by [D4vRAM369](https://github.com/D4vRAM369)

