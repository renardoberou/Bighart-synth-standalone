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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signing is configured in Phase B (see PLAN.md). Until then,
            // `assembleRelease` produces an unsigned artifact and CI builds debug.
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
