# 100 Days

Local-first Android app for tracking daily progress toward a deadline. A GitHub-style contribution grid drives both the in-app home and (soon) a Glance home-screen widget. Journal entries stay on device in Room. No accounts. No sync in v1.

## Status

Active development. Architecture package is under `docs/design/`. First vertical slice targets Room CRUD, onboarding, in-app grid, and day entry editing.

## Stack

- Kotlin, Jetpack Compose, Room, DataStore
- Glance widget and on-device voice cleanup come in later commits
- Companion PWA is a separate codebase and not required for Android v1

## Build

```bash
# JDK 17 required
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Android SDK path lives in `local.properties` (gitignored).

## License

MIT. See `LICENSE`.
