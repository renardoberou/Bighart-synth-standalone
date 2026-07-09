# The Bighart — Android

A standalone native Android build of **The Bighart**, a 3-VCO analog-style
synthesizer with a triple filter, tape delay, and shimmer reverb. The instrument
runs fully offline inside a thin WebView shell; the audio engine is the Web Audio
implementation from the Resonant Systems browser build, bundled byte-identical
as an offline asset.

## Status

**Phase A and Phase B complete. v1.0.0 signed release live and developer-tested.**

| | |
|---|---|
| applicationId | `com.resonantsystems.bighart` (immutable after first release) |
| minSdk / targetSdk / compileSdk | 26 / 35 / 35 |
| versionCode / versionName | 1 / `1.0` |
| Build | AGP 8.6.1 · Kotlin 2.0.20 · Gradle 8.10.2 |
| Debug CI | ✅ Green — [run 28971582382](https://github.com/renardoberou/Bighart-synth-standalone/actions/runs/28971582382), `bighart-debug-apk` artifact produced, verified via the GitHub API. |
| Release CI | ✅ Green — [run 28981239727](https://github.com/renardoberou/Bighart-synth-standalone/actions/runs/28981239727), triggered by the `v1.0.0` tag. |
| Signed release | ✅ [v1.0.0](https://github.com/renardoberou/Bighart-synth-standalone/releases/tag/v1.0.0) — `app-release.apk` (2,745,945 bytes), `app-release.aab` (2,504,776 bytes), `SHA256SUMS.txt`, all confirmed present via the GitHub API. |
| On-device test | ✅ **Developer-confirmed**: installed and tested the signed `v1.0.0` build — "sounds/works great." (Reported by the developer; not independently verified by an agent, which has no device access.) |
| Known limitation | Android WebView does not implement Web MIDI, so the synth's in-app MIDI LEARN panel loads but external MIDI controllers will not connect. Touch play is fully functional. A native MIDI bridge is scoped as Phase C. |
| Internet permission | Not present — the app is fully offline. |

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

Core app is release-ready: signed `v1.0.0` build exists, is checksummed, and
has been installed and tested on-device by the developer. Remaining steps are
distribution, not engineering:
1. Gumroad listing (direct APK/AAB channel).
2. Google Play: internal testing track first (not production) — requires a
   privacy policy and, on a new Play developer account, 12 opted-in testers
   for 14 continuous days before production access.
3. `apksigner verify --verbose` on the release APK is still worth running
   once before any public distribution link goes out, as a final integrity
   check independent of the developer's functional smoke test.

No keystore, signing credentials, `.env` file, APK/AAB, or Play Console data is
stored in this repository — see `.gitignore`.
