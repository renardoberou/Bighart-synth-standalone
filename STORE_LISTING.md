# Play Store submission — draft materials

Everything in this file is a **draft for you to review and paste into Play
Console** — nothing here has been submitted anywhere. Play Console itself
isn't reachable by an agent (no API/connector for it), so this is the
handoff document.

## Confirmed requirements (checked against Google's own docs, July 2026)

- **Target API level:** current official requirement is Android 15 / API 35
  for new app submissions ([developer.android.com, updated 2026‑04‑22](https://developer.android.com/google/play/requirements/target-sdk)).
  This app already targets **API 35** — compliant today. There's third-party
  chatter about an API 36 requirement landing August 31, 2026; worth a quick
  recheck of Play Console's own notice closer to that date, but it does not
  block submission now.
- **Closed testing gate:** confirmed via [Play Console Help](https://support.google.com/googleplay/android-developer/answer/14151465) —
  personal developer accounts created after November 13, 2023 must run a
  closed test with **at least 12 opted-in testers, continuously opted in for
  14 days**, before production access unlocks. You've confirmed your account
  falls in this bucket, so this gate applies here.
- **Format:** Play requires the `.aab` (App Bundle), which the release
  workflow already produces (`app-release.aab`, from the `v1.0.0` GitHub
  Release).
- **Play App Signing:** recommended — let Google re-sign the app for
  distribution while you keep your own upload key. Enroll during the
  first production upload in Play Console; no repo changes needed either way.

## What's ready

- ✅ Signed `app-release.aab` — [v1.0.0 release](https://github.com/renardoberou/Bighart-synth-standalone/releases/tag/v1.0.0)
- ✅ Hi-res icon — `store/play/icon-512.png` (512×512, RGBA)
- ✅ Feature graphic — `store/play/feature-graphic-1024x500.png` (1024×500)
- ✅ Privacy policy — drafted and pushed to the portfolio site:
  `https://renardoberou.github.io/portfolio-site/privacy-policy.html`
- ✅ Store listing copy — draft below
- ✅ Data Safety form answers — draft below, derived directly from the
  manifest (no INTERNET permission = no network transmission possible)

## What's NOT ready / needs you specifically

- ❌ **Screenshots.** Play requires at least 2 (up to 8) real screenshots,
  JPEG or 24-bit PNG (no alpha), each dimension between 320px and 3840px,
  16:9 or 9:16. These have to come from your actual device — I have no way
  to capture your running app. Suggested shots: the main synth UI, the tape
  delay/reverb panel open, the MIDI learn panel, a knob being adjusted.
- ❌ **12 testers / 14 days closed test.** You need to actually recruit and
  run this — friends, family, or a community, per Google's own recruiting
  guidance. Budget the 14 days into your timeline; it cannot be shortened
  (see PLAN.md/RELEASE.md — this is now the long pole).
- ❌ **Content rating questionnaire** — answered interactively in Play
  Console. Expected answers for this app (a synthesizer instrument, no user-
  generated content, no violence/gambling/user communication): should land
  at the lowest tier (Everyone / PEGI 3), but you'll need to actually answer
  Google's questionnaire yourself since it's account-specific.
- ❌ **Play Console account details** — app category, contact details,
  target countries, pricing (free vs. paid — the portfolio pricing precedent
  elsewhere is $12; decide if that applies here or if this ships free).

## Store listing copy (draft)

**App name:** The Bighart

**Short description** (≤80 characters, currently 78):
```
Offline analog-style synthesizer — 3 VCOs, triple filter, delay, reverb.
```

**Full description** (≤4000 characters):
```
The Bighart is a standalone, fully offline analog-style synthesizer.
Three voltage-controlled oscillators, a triple filter section, tape-style
delay, and a shimmer reverb — all running on-device with no internet
connection required.

FEATURES
• 3-VCO analog-style synthesis engine
• Triple filter section for shaping tone
• Built-in tape delay and shimmer reverb
• Touch-based keyboard and pads
• Save and recall your own presets, stored privately on your device
• Fully offline — no account, no ads, no tracking, no data collection

WHO IT'S FOR
Musicians, sound designers, and anyone who wants a pocket-sized analog-
style synth for sketching ideas, live play, or just exploring sound.

PRIVACY
The Bighart doesn't request internet access and can't transmit anything
even if it wanted to. Presets are saved locally on your device only.
See the full privacy policy for details.

Part of the Resonant Systems collection of browser- and Android-native
audio instruments.
```

**Category:** Music & Audio

**Contact email:** roustandsystems@gmail.com

**Privacy policy URL:** `https://renardoberou.github.io/portfolio-site/privacy-policy.html`

## Data Safety form — draft answers

Derived directly from the manifest (no `INTERNET` permission present) and
the WebView configuration (no analytics/ad SDKs bundled):

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | N/A — no data leaves the device |
| Do you provide a way for users to request data deletion? | N/A — no data is collected |
| Data types collected | None |
| Data shared with third parties | None |
| Ads | No |
| Third-party analytics/crash SDKs | None bundled |

If a future version adds anything network-related (the MIDI Phase C work,
for instance, is still local Bluetooth/USB, not internet — but re-check this
table if that changes), update this table and the privacy policy together
before that version ships.
