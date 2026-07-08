plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.resonantsystems.bighart"
    compileSdk = 35

    defaultConfig {
        // IMMUTABLE after the first public release — do not change.
        applicationId = "com.resonantsystems.bighart"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Phase B: release signing.
    //
    // Deliberately env-var driven rather than reading a keystore.properties
    // file, so the same build.gradle.kts works unmodified whether the four
    // RELEASE_* variables come from a local shell (manual signing) or from
    // GitHub Actions secrets (CI release build — see
    // .github/workflows/release.yml). No secret material or path is
    // hardcoded here or in any committed file.
    //
    // When RELEASE_KEYSTORE_PATH is absent (local debug work, PR builds),
    // the release build type below falls back to unsigned, exactly as
    // before — nothing breaks for anyone without the four variables set.
    signingConfigs {
        create("release") {
            val path = System.getenv("RELEASE_KEYSTORE_PATH")
            if (path != null) {
                storeFile = file(path)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            // If RELEASE_* env vars are unset, this produces an unsigned
            // artifact (same as before Phase B) rather than failing the build.
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // Keep the bundled HTML/JS untouched (do not let aapt compress or mangle it).
    androidResources {
        noCompress += listOf("html", "js", "json")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    // WebViewAssetLoader: serves bundled assets from a stable https origin so
    // Web Audio / localStorage behave as a secure context (presets persist).
    implementation("androidx.webkit:webkit:1.11.0")
}
