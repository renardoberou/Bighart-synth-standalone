# The Bighart — Android

A standalone native Android build of **The Bighart**, a 3-VCO analog-style
synthesizer with a triple filter, tape delay, and shimmer reverb. The instrument
runs fully offline inside a thin WebView shell; the audio engine is the Web Audio
implementation from the Resonant Systems browser build, bundled byte-identical
as an offline asset.

## Status

**Phase A (scaffold) complete. Not yet verified on-device.**

| | |
|---|---|
| applicationId | `com.resonantsystems.bighart` (immutable after first release) |
| minSdk / targetSdk / compileSdk | 26 / 35 / 35 |
| versionCode / versionName | 1 / `1.0` |
| Build | AGP 8.6.1 · Kotlin 2.0.20 · Gradle 8.10.2 |
| CI | GitHub Actions → `:app:assembleDebug` → `bighart-debug-apk` artifact on every push to `main` |
| Signing | **Not configured** — CI produces a debug APK only. See PLAN.md Phase B. |
| On-device install/test | **Not yet confirmed.** An earlier upload attempt landed the project with a flattened file structure (all files at repo root, no `.github/workflows/`), which meant CI never ran and no APK was ever built or installed from this repo. This commit corrects the structure. Next step is a real CI run + install. |
| Known limitation | Android WebView does not implement Web MIDI, so the synth's in-app MIDI LEARN panel loads but external MIDI controllers will not connect. Touch play is fully functional. A native MIDI bridge is scoped as Phase C. |

See **PROGRESS.md** for the live log and **PLAN.md** for the full architecture and phased roadmap.

## Build

CI builds a debug APK on every push to `main` (`.github/workflows/android.yml`)
and uploads it as the **`bighart-debug-apk`** artifact — check the Actions tab.

Locally (requires JDK 17 + network access to Google's Maven and the Gradle
distribution service):

```sh
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

## Project layout

```
app/src/main/assets/index.html          — the synth (canonical, offline asset)
app/src/main/java/.../MainActivity.kt   — WebView shell
app/src/main/AndroidManifest.xml        — no INTERNET permission (fully offline)
app/build.gradle.kts                    — module config, applicationId, versions
.github/workflows/android.yml           — CI: build + upload debug APK
PLAN.md                                 — architecture + phased roadmap
PROGRESS.md                             — live status log for handoff between sessions/agents
```

## Release readiness

Not release-ready. Before any public release:
1. First green CI run on this corrected structure (unverified as of this commit).
2. On-device install + smoke test (audio, all knobs/pads, tape delay, reverb,
   preset save/reload via localStorage, rotation, insets on a notched device).
3. Phase B: generate an upload keystore, add signing config + CI secrets,
   produce a signed release build.

No keystore, signing credentials, `.env` file, APK/AAB, or Play Console data is
stored in this repository — see `.gitignore`.
