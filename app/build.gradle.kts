plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.prempk.personalai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prempk.personalai"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
        // Redmi Note 9 uses ARM; exclude unused x86 native runtimes from the install APK.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    // Use a persistent CI signing identity when supplied. Without it, Gradle's
    // per-run debug key changes and Android rejects APK updates as signature conflicts.
    val stableSigningStorePath = System.getenv("PK_SIGNING_KEYSTORE_PATH")
    if (!stableSigningStorePath.isNullOrBlank()) {
        signingConfigs {
            create("stableDebug") {
                storeFile = file(stableSigningStorePath)
                storePassword = System.getenv("PK_SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("PK_SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("PK_SIGNING_KEY_PASSWORD")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("debug") {
            if (!stableSigningStorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("stableDebug")
            }
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(files("libs/sherpa-onnx-2.0.1.aar"))
    implementation("org.apache.commons:commons-compress:1.27.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
