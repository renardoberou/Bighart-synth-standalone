# The Bighart — Native Android App · PLAN

**Project:** Wrap the self-contained Web Audio synthesizer *The Bighart* as a
standalone, installable Android app (APK), built end-to-end on GitHub Actions
CI. No local toolchain required — same mobile-only, CI-driven workflow used for
**Bighart Beat** and **The Ping Thing**.

**Package / applicationId:** `com.resonantsystems.bighart` — **IMMUTABLE after
first public release.** Changing it later means a brand-new Play/Gumroad listing
and orphaned installs. Decided now; do not edit in `app/build.gradle.kts`.

---

## What "native, not webapp" means here

The audio engine is the proven WebAudio implementation already shipping in the
browser build. We do **not** rewrite it in Kotlin/Oboe — that would throw away a
working, tuned instrument for no user-visible gain. Instead we produce a **real
installable native package** (APK) that:

- bundles the entire synth as an **offline asset** (no server, no network),
- runs it in a single hardware-accelerated `WebView`,
- and adds the native behaviours a standalone instrument needs (screen-on,
  audio focus, edge-to-edge + cutout insets, rotation without audio dropout,
  a launcher icon, an app identity).

This is the identical architecture pattern as Bighart Beat. The difference from
Beat is that a **synth has no transport** to expose in a media notification, so
there is no `PlaybackService` / `S.playing` bridge in v1 (see Phase C for the
optional background-drone case).

---

## Architecture

```
APK
└─ MainActivity (single Activity, no Compose)
   └─ WebView (hardware-accelerated, JS + DOM storage enabled)
      └─ WebViewAssetLoader  →  https://appassets.androidplatform.net/assets/index.html
         └─ index.html  (the synth, byte-identical to the browser build)
```

Key decisions and *why*:

| Decision | Reason |
|---|---|
| **WebViewAssetLoader** (https origin) not `file://` | Gives the page a **secure context** so Web Audio is unrestricted and `localStorage` presets **persist** across launches under a stable origin. `file://` origins are opaque and lose storage. |
| **`mediaPlaybackRequiresUserGesture = false`** | The synth starts its AudioContext on first touch anyway, but this removes WebView's extra autoplay gate. |
| **`domStorageEnabled = true`** | The synth saves presets/MIDI-map to `localStorage` (9 call sites). |
| **`configChanges=...` on the Activity** | Rotation does **not** recreate the Activity, so the WebView (and its live AudioContext) is never destroyed mid-performance. |
| **`FLAG_KEEP_SCREEN_ON`** | An instrument shouldn't sleep while you're playing it. |
| **No `INTERNET` permission** | Fully offline/standalone. The only remote ref in `index.html` is a Google Fonts `@import` (Zilla Slab), which degrades cleanly to the Courier-New fallback the design already uses. Add the permission back to restore the webfont. |
| **`index.html` left byte-identical** | So it stays diff-able against the canonical browser source. All native concerns live in Kotlin/manifest, nothing in the HTML. |
| **Adaptive vector launcher icon** | No binary PNGs to manage; a "big heart" glyph (`#F5890A` on `#2A2820`). |
| **Gradle wrapper committed** | `./gradlew` works on CI with zero provisioning. Pinned Gradle 8.10.2 / AGP 8.6.1 / Kotlin 2.0.20 / compileSdk 35 / minSdk 26 — same baseline as Beat. |

### Known limitation — Web MIDI
`index.html` calls `navigator.requestMIDIAccess`. **Android System WebView does
not implement Web MIDI**, so the in-app MIDI LEARN panel will load but external
controllers won't connect in v1. The synth is fully playable by touch. A native
MIDI bridge is **Phase C** (see below) and is the single biggest native-only
value-add available for this app.

---

## Phased delivery

### Phase A — Green APK (scope of this pass)  ✅ built, ⏳ needs on-device test
- [x] Project scaffold: Gradle (KTS), AGP 8.6.1 / Kotlin 2.0.20, wrapper committed.
- [x] `MainActivity` WebView shell + WebViewAssetLoader.
- [x] Manifest: launcher activity, no INTERNET, rotation-safe configChanges,
      edge-to-edge + cutout, keep-screen-on.
- [x] Offline asset bundling (`noCompress` html/js/json), chassis-coloured
      background (no white flash), adaptive icon.
- [x] CI workflow → `assembleDebug` → uploads `bighart-debug-apk` artifact.
- [ ] **First CI run is green** (verify in Actions tab).
- [ ] **On-device smoke test**: install the artifact, confirm audio, touch
      keyboard/pads, knobs, tape delay/reverb, preset save+reload (persistence),
      rotation keeps audio alive, no white flash, insets correct on a notch device.

### Phase B — Signing & release
- [ ] Generate an upload keystore (`keytool`, e.g. via Termux). Record alias +
      passwords in a password manager. **Back up the keystore** — losing it means
      you can never update the listing.
- [ ] Add repo **secrets**: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
      `KEY_ALIAS`, `KEY_PASSWORD`.
- [ ] Add a `signingConfigs { release { ... } }` block reading those (via
      `keystore.properties` locally / env on CI) and wire `buildTypes.release`.
- [ ] Add a tag-triggered CI job (`push: tags: v*`) → decode keystore →
      `assembleRelease` → attach signed APK to a GitHub Release.
- [ ] Gumroad listing (mirror the Resonant Systems store; Bighart price point
      per existing pricing: Bighart $12).

### Phase C — Optional native enhancements
- [ ] **Native MIDI bridge** (the headline feature): use `android.media.midi`
      (`MidiManager`) to enumerate USB/BLE MIDI devices, and a small
      `@JavascriptInterface` bridge that injects note/CC events into the synth's
      existing MIDI handler (the page already has `learnableParams` + a CC map —
      feed it synthetic `MIDIMessageEvent`-shaped objects, or expose a
      `window.__nativeMidi(status,d1,d2)` hook the page listens to). This makes
      external controllers work where Web MIDI can't.
- [ ] **Background drone**: if a drone is sustaining, a lightweight foreground
      service + media-style notification (with a single STOP action) keeps audio
      alive when backgrounded. Needs a JS→native bridge reporting drone on/off
      (mirror Beat's `Object.defineProperty` interception pattern).
- [ ] **Vendored webfont** for true-offline Zilla Slab (drop the Google Fonts
      `@import`, ship a local `woff2` + `@font-face`) — only if the typography
      difference matters.
- [ ] Low-latency audio path review (AAudio/`sustained-performance` window) if
      latency is judged too high in testing.

---

## How another agent should pick this up
1. Read **PROGRESS.md** for the live status / last action / open questions.
2. The synth source is `app/src/main/assets/index.html` — treat as canonical &
   diff-able; do not edit it to solve native problems.
3. All build knobs: `app/build.gradle.kts` (versions, applicationId, noCompress)
   and `app/src/main/AndroidManifest.xml` (permissions, activity flags).
4. CI: `.github/workflows/android.yml`. The debug APK lands as the
   `bighart-debug-apk` artifact on every push to `main`.
5. Update PROGRESS.md when you finish a step.
