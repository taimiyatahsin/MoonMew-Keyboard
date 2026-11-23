plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseStoreFile = System.getenv("MOONMEW_RELEASE_STORE_FILE")
val releaseStorePassword = System.getenv("MOONMEW_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = System.getenv("MOONMEW_RELEASE_KEY_ALIAS")
val releaseKeyPassword = System.getenv("MOONMEW_RELEASE_KEY_PASSWORD")
val releaseSigningValues = listOf(
    releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
)
val releaseSigningConfigured = releaseSigningValues.all { !it.isNullOrBlank() }
require(releaseSigningValues.all { it.isNullOrBlank() } || releaseSigningConfigured) {
    "Release signing requires all four MOONMEW_RELEASE_* environment variables."
}

android {
    namespace = "com.moonmew.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moonmew.keyboard"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            // Keep release shrinking disabled until a measured, tested R8 rule set exists.
            isMinifyEnabled = false
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

