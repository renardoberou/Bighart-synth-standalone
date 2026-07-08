# The Bighart — Android

A standalone native Android build of **The Bighart**, a 3-VCO analog-style
synthesizer with a triple filter, tape delay, and shimmer reverb. The instrument
runs fully offline inside a thin WebView shell; the audio engine is the Web Audio
implementation from the Resonant Systems browser build, bundled byte-identical
as an offline asset.

## Status

**Phase A complete and verified. Phase B (signing) in progress.**

| | |
|---|---|
| applicationId | `com.resonantsystems.bighart` (immutable after first release) |
| minSdk / targetSdk / compileSdk | 26 / 35 / 35 |
| versionCode / versionName | 1 / `1.0` |
| Build | AGP 8.6.1 · Kotlin 2.0.20 · Gradle 8.10.2 |
| Debug CI | ✅ Green — [run 28971582382](https://github.com/renardoberou/Bighart-synth-standalone/actions/runs/28971582382), commit `ad56913`, `bighart-debug-apk` artifact produced (2.99 MB), verified via the GitHub API. |
| Release CI | Added (`.github/workflows/release.yml`, tag-triggered on `v*`) but not yet run — needs the four signing secrets first. See RELEASE.md. |
| Signing | Env-var-driven signing config added to `app/build.gradle.kts`. Keystore not yet generated — that step is intentionally left to the developer (see RELEASE.md), not automated by an agent, so the private key never passes through a chat transcript. |
| On-device install | ✅ Confirmed installed and launching (Motorola Edge 60 Fusion). Full control-by-control smoke test (tape delay, reverb, all knobs/pads, preset save/reload, rotation, notch insets) — see PROGRESS.md for exactly which items have been explicitly confirmed vs. still open. |
| Known limitation | Android WebView does not implement Web MIDI, so the synth's in-app MIDI LEARN panel loads but external MIDI controllers will not connect. Touch play is fully functional. A native MIDI bridge is scoped as Phase C. |
| Internet permission | Not present — the app is fully offline. (Confirm this remains accurate if any feature is later added that needs network.) |

See **PROGRESS.md** for the live log, **PLAN.md** for the full architecture and phased roadmap, and **RELEASE.md** for the signing/secrets steps.

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

Not release-ready yet. Remaining before any public release:
1. Generate the release keystore + load the four GitHub secrets (RELEASE.md) — developer step, not automated.
2. Cut `v1.0.0`, confirm `.github/workflows/release.yml` produces a signed, checksummed APK + AAB.
3. `apksigner verify` the output; on-device smoke test of the *signed* build specifically.
4. Only then: Gumroad listing / Google Play internal testing track.

No keystore, signing credentials, `.env` file, APK/AAB, or Play Console data is
stored in this repository — see `.gitignore`.
