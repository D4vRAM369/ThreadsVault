# Contributing

Thanks for your interest in contributing to ThreadsVault.

## Setup

1. Use Android Studio stable.
2. Use JDK 17.
3. Install Android SDK 35.
4. Run:

```bash
./gradlew :app:assembleDebug
```

## Workflow

1. Fork the repository.
2. Create a feature branch:

```bash
git checkout -b feat/your-change
```

3. Keep PRs focused and small.
4. Add/adjust tests when behavior changes.
5. Ensure project compiles before opening PR.

## Code style

- Kotlin + Jetpack Compose conventions.
- Keep UI logic readable and composables focused.
- Prefer clear naming over clever abstractions.

## Commit messages

Use short, descriptive commits. Example:

`feat(vault): add about dev entry in settings`

## Pull requests

- Explain what changed and why.
- Include screenshots for UI changes.
- Mention any known limitations.
