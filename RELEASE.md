# Release signing — Phase B

This is the one part of the release pipeline that intentionally is **not**
automated by an agent: generating the private release keystore and loading it
into GitHub as secrets. Do this from Termux, on-device. Nothing here should
ever be pasted into a chat, an issue, a commit, or a log.

## 1. Generate the keystore (Termux, outside any repo checkout)

```bash
pkg install openjdk-17
SIGNING_DIR="$HOME/.local/share/android-signing/bighart-synth-standalone"
mkdir -p "$SIGNING_DIR" && chmod 700 "$SIGNING_DIR"
cd "$SIGNING_DIR"

keytool -genkeypair -v \
  -keystore bighart-synth-standalone-release.jks \
  -alias bighart-release \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Resonant Systems, O=Resonant Systems"

keytool -list -v \
  -keystore bighart-synth-standalone-release.jks \
  -alias bighart-release \
  > bighart-synth-standalone-certificate-fingerprint.txt
```

Then:
- Save the keystore password in a password manager.
- Copy the `.jks` to an offline backup (a second location you control —
  losing this file means you can never update the app under this signature).
- Keep the fingerprint file private.

## 2. Load the four secrets into GitHub (still in Termux, `gh` CLI)

```bash
cd "$HOME/.local/share/android-signing/bighart-synth-standalone"
base64 -w0 bighart-synth-standalone-release.jks > bighart-synth-standalone-keystore.b64

gh secret set KEYSTORE_B64  --repo renardoberou/Bighart-synth-standalone < bighart-synth-standalone-keystore.b64
gh secret set KEYSTORE_PASS --repo renardoberou/Bighart-synth-standalone
gh secret set KEY_ALIAS     --repo renardoberou/Bighart-synth-standalone --body "bighart-release"
gh secret set KEY_PASS      --repo renardoberou/Bighart-synth-standalone

rm bighart-synth-standalone-keystore.b64   # don't leave the base64 copy lying around
gh secret list --repo renardoberou/Bighart-synth-standalone   # confirm names only, never values
```

Required secret names (must match exactly — `.github/workflows/release.yml`
reads these):
```
KEYSTORE_B64
KEYSTORE_PASS
KEY_ALIAS
KEY_PASS
```

## 3. Cut the release

Once the four secrets exist:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers `.github/workflows/release.yml`, which decodes the keystore
in-memory on the runner only, builds `app-release.apk` and `app-release.aab`,
computes SHA-256 checksums, deletes the decoded keystore file unconditionally,
and attaches everything to a GitHub Release.

## 4. Verify before publishing any link

```bash
apksigner verify --verbose path/to/app-release.apk
sha256sum path/to/app-release.apk path/to/app-release.aab
```

## 5. Smoke-test before calling it released

- Confirm launch from the home screen (already confirmed for the debug build).
- Confirm audio actually plays (Web Audio starts after a user gesture, as the
  synth already expects).
- Confirm preset save/reload persists (localStorage under the WebView asset
  origin).
- Confirm no crash across rotate → background → foreground.
- Only after this, update README.md's status table and write release notes.

## What never gets committed

`.jks`, `.keystore`, `.aab`, `.apk`, `keystore.properties`, `.env`, the
base64-encoded keystore, or the certificate fingerprint file. `.gitignore`
already excludes the binary/keystore extensions; the fingerprint/base64 files
above are one-off working files in `$SIGNING_DIR`, outside any repo checkout,
and should be deleted after use per the commands above.
