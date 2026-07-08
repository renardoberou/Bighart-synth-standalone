# The app is a thin WebView shell; no reflection-heavy code. Keep defaults.
# If R8/minify is enabled later (Phase B/C), preserve any @JavascriptInterface
# bridge methods so they survive obfuscation:
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
