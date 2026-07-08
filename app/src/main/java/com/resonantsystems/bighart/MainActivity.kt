package com.resonantsystems.bighart

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.webkit.WebViewAssetLoader

/**
 * The Bighart — native Android shell.
 *
 * Hosts the self-contained Web Audio synthesizer (app/src/main/assets/index.html)
 * inside a single WebView. Assets are served by WebViewAssetLoader from a stable
 * https origin (https://appassets.androidplatform.net/assets/), which gives the
 * page a secure-context so Web Audio behaves and localStorage presets persist
 * across launches.
 *
 * Audio engine = the proven WebAudio implementation. This activity adds the
 * native concerns a standalone instrument needs: edge-to-edge layout with
 * display-cutout insets exposed to CSS env(safe-area-inset-*), screen kept awake
 * while playing, audio focus, and rotation handled without tearing down the
 * WebView (see android:configChanges in the manifest) so audio never drops.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the system bars; the HTML already respects safe-area insets.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        // Keep the screen awake during a performance.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView = WebView(this).apply {
            // Make the WebView background match the instrument chassis so there is
            // no white flash on cold start.
            setBackgroundColor(0xFF141208.toInt())

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true            // localStorage for presets
                mediaPlaybackRequiresUserGesture = false
                allowFileAccess = false             // not needed; assets via loader
                allowContentAccess = false
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                // Keep all navigation inside the shell.
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean = true
            }
        }

        // Remote debugging only in debuggable builds.
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        WebView.setWebContentsDebuggingEnabled(debuggable)

        setContentView(webView)
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    /** Be a polite audio citizen: request focus while in the foreground. */
    private fun requestAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { }
                .build()
            focusRequest = req
            am.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        requestAudioFocus()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        // v1 is foreground-only; background audio (drone) is a Phase C feature.
        abandonAudioFocus()
    }

    @Suppress("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        // A single screen — let back exit to home rather than unloading the synth.
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
