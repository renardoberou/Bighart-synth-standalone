# PROGRESS

Live status log. **Append an entry whenever you finish a step.** Newest first.

## Status board
- **Phase A — Green APK:** scaffold complete, awaiting first CI run + on-device smoke test.
- **Phase B — Signing & release:** not started.
- **Phase C — Native MIDI / background drone:** not started.

## Open questions / decisions to confirm
1. **applicationId `com.resonantsystems.bighart`** — confirm this is the desired
   final ID before any public release (it is immutable afterward).
2. **versionName `1.0`** — fine to ship as the first release? Bump in
   `app/build.gradle.kts` if you want a `0.9` beta first.
3. **Webfont** — keep fully-offline (current: Zilla Slab falls back to Courier
   New) or add INTERNET + load the webfont? Default chosen: offline.
4. **MIDI** — is external-controller support needed for v1, or is touch-only
   acceptable? (Web MIDI won't work in WebView; native bridge is Phase C.)

## Log

### 2026-07-09 (later) — Play Store submission prep
- Verified current requirements against Google's own docs (not third-party
  blogs, several of which incorrectly imply an already-active API 36
  requirement — that deadline is Aug 31, 2026 and doesn't block today):
  target API 35 is the live requirement for new submissions, confirmed via
  developer.android.com (updated 2026‑04‑22); this app already targets 35.
- Confirmed via Play Console Help: the 12-tester/14-continuous-day closed
  testing gate applies to personal accounts created after Nov 13, 2023.
  Developer confirmed their account falls in this bucket — this is now the
  long pole on the Play timeline, not anything code-side.
- Generated Play Store visual assets from the existing waveform artwork
  (same source as the launcher icon): `store/play/icon-512.png` (512×512
  RGBA, full-bleed, not safe-zone-cropped like the adaptive launcher icon)
  and `store/play/feature-graphic-1024x500.png`.
- Drafted `STORE_LISTING.md`: store listing copy (title/short/full
  description), Data Safety form answers (derived directly from the
  manifest — no INTERNET permission, so "no data collected" is a
  verifiable fact, not a guess), and an explicit split of what's ready vs.
  what only the developer can do (screenshots, the 14-day tester recruitment,
  the content rating questionnaire, Play Console account-level settings).
- Also touched `renardoberou/portfolio-site` (separate repo, same PAT
  happened to have access — verified via API before assuming, since the
  token was originally requested scoped to this repo only):
  - Added `privacy-policy.html`, matching the site's existing visual style,
    covering all Resonant Systems apps generically with Bighart named as the
    current example. Live at
    https://renardoberou.github.io/portfolio-site/privacy-policy.html
    (Pages already configured, serving from `main`/root — confirmed via API,
    not assumed).
  - Linked it from the homepage footer.
  - Fixed a stale status line (all 3 languages) that still said "phone smoke
    test pending" for Bighart Synth — no longer true since the signed
    v1.0.0 on-device test. Updated to reflect the actual current state.
- **NOT done, by design:** nothing was submitted to Play Console — there's
  no API/connector access to it from this environment. This is a document
  handoff, not a submission.

### 2026-07-09 — Signed v1.0.0 build tested on-device (developer-confirmed)
- Developer installed and tested the signed `v1.0.0` release build (the one
  from GitHub Release `v1.0.0`, not the earlier debug artifact) and reported:
  "tested everything, it sounds/works great."
- Recording this precisely as what it is — a developer-reported functional
  test, not something independently verified by an agent (no device access
  exists in this pipeline). Marked as such in PLAN.md/README.md rather than
  stated as agent-verified fact.
- With this, Phase B's engineering scope is complete: signing config, tag-
  triggered release CI, the actual keystore/secrets, the `v1.0.0` signed
  artifacts, and now a positive on-device test. Everything remaining
  (Gumroad, Play Store internal testing) is distribution/store-listing work,
  not code or CI.
- Still outstanding, noted rather than silently dropped: an independent
  `apksigner verify --verbose` pass on the release APK hasn't been run by
  either the agent or confirmed by the developer as run — worth doing once
  before any public link goes out, as a check that's orthogonal to "does it
  sound good."

### 2026-07-08 (later) — CI verified green; Phase B signing scaffolding added
- **CI verified, not just assumed:** pushed the corrected tree (commit
  `ad56913`) via a fine-grained PAT scoped to this repo only (Contents +
  Workflows: read/write). Confirmed via GitHub API, not just push output:
  HEAD sha matches, tree structure matches, workflow run `28971582382`
  completed with `conclusion: success`, and the `bighart-debug-apk` artifact
  exists at 2,990,298 bytes and is not expired.
- Developer confirmed the app is installed and launching on-device (Motorola
  Edge 60 Fusion). Noting explicitly: "installed and launches" is confirmed;
  a full control-by-control smoke test (every knob/pad, tape delay, reverb,
  preset save/reload persistence, rotation, notch insets) has **not** been
  itemized yet — add specific confirmations here as each is actually run,
  rather than let "it's working" quietly expand to cover everything.
- **Phase B started — code/CI side only:**
  - `app/build.gradle.kts`: added `signingConfigs { create("release") }`
    reading four `RELEASE_*` environment variables; falls back to unsigned
    when they're absent so debug/PR builds are unaffected.
  - `.github/workflows/release.yml`: new tag-triggered (`v*`) workflow —
    decodes `KEYSTORE_B64` secret to a runner-temp file, builds
    `assembleRelease` + `bundleRelease`, computes SHA-256 checksums, deletes
    the decoded keystore unconditionally (`if: always()`), attaches
    APK+AAB+checksums to a GitHub Release.
  - `RELEASE.md`: new file — the keystore generation + `gh secret set` steps,
    written for the developer to run in Termux. **Deliberately not automated
    by the agent** — the private key and password should never pass through
    a chat transcript or an agent sandbox, only device → GitHub secrets
    store directly.
  - `PLAN.md` / `README.md`: updated to reflect the above and to distinguish
    "installed on-device" (confirmed) from "fully smoke-tested" (not yet
    itemized) and from "signed release exists" (not yet — no keystore/secrets
    created yet).
- **NOT yet done:** keystore generation, GitHub secrets (`KEYSTORE_B64`,
  `KEYSTORE_PASS`, `KEY_ALIAS`, `KEY_PASS`), the `v1.0.0` tag push, the
  release workflow's first run, `apksigner verify`, Gumroad/Play listings.

### 2026-07-08 — Repo structure corrected (re-sync)
- Root cause of "no CI run" confirmed: the initial upload via GitHub web UI
  flattened the entire tree to the repo root (single commit, 20 files, no
  `.github/workflows/`, no `.gitignore`, a colliding `build.gradle (1).kts`).
  GitHub only discovers workflows at `.github/workflows/*.yml`, so no Actions
  run was ever possible from that state — nothing was actually broken in the
  Kotlin/Gradle content itself, only its location.
- Re-synced the full correct tree from this conversation's working build:
  `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`,
  `app/src/main/java/com/resonantsystems/bighart/MainActivity.kt`,
  `app/src/main/assets/index.html` (verified byte-identical to source, sha256
  `794169d5deb7653bde1de3441e58498b4b176c545b4f568ab07a926bea1a2839`),
  `app/src/main/res/**`, root Gradle files, wrapper jar, `gradlew`/`gradlew.bat`.
  Restored `.github/workflows/android.yml` and `.gitignore`.
- Removed the duplicate `build.gradle (1).kts` artifact — a single
  `app/build.gradle.kts` is now canonical.
- Rewrote README.md status section to state plainly that on-device install/test
  is **not yet confirmed** from this repo (no prior commit ever produced a
  working CI run), rather than assume the phone install succeeded.
- Verified no secrets, keystores, `.env` files, APKs/AABs, or local logs are
  present or tracked (`.gitignore` covers `*.jks`, `*.keystore`,
  `keystore.properties`, `signing.properties`, `build/`, `local.properties`).
- Confirmed unchanged: applicationId `com.resonantsystems.bighart`, minSdk 26,
  targetSdk 35, versionCode 1, versionName `1.0` — no drift from Phase A.
- **Could not run a real Gradle build in the agent sandbox** — this environment's
  network allowlist doesn't include Google's Maven or the Gradle distribution
  service, only GitHub/npm/PyPI/crates mirrors. Static checks only (XML
  validation, path/consistency review, byte-diff of the asset). The real build
  check is the GitHub Actions run this push will trigger.
- **NOT yet done:** confirming the Actions run is green, on-device install,
  Phase A smoke-test checklist, signing (Phase B).

### 2026-06-25 — Phase A scaffold created
- Generated full Gradle project wrapping `the-bighart__6_.html` as the offline
  WebView asset (`app/src/main/assets/index.html`, copied byte-identical).
- `MainActivity.kt`: WebView + WebViewAssetLoader (https asset origin), DOM
  storage on, autoplay gate off, edge-to-edge + display-cutout, keep-screen-on,
  audio focus on resume/abandon on pause, rotation handled via manifest
  configChanges (no Activity recreation → audio survives rotation).
- Manifest: launcher activity, **no INTERNET permission** (offline), singleTask,
  back = move-to-back (don't unload synth).
- Resources: chassis-colour theme/background (no white flash), adaptive vector
  launcher icon ("big heart", #F5890A on #2A2820).
- Build: AGP 8.6.1 / Kotlin 2.0.20 / compileSdk 35 / targetSdk 35 / minSdk 26,
  `noCompress` for html/js/json, Gradle wrapper 8.10.2 committed.
- CI: `.github/workflows/android.yml` → `:app:assembleDebug` → artifact
  `bighart-debug-apk`.
- **NOT yet done:** first CI run verification; on-device smoke test; signing.

### Next action
Push to a new `renardoberou` repo (suggested name **`Bighart`** or
`bighart-synth`), let CI build, download the `bighart-debug-apk` artifact from
the Actions run, install on device, and run the Phase A smoke-test checklist in
PLAN.md. Record results here.
