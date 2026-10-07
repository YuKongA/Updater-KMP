@file:Suppress("UnstableApiUsage")

import java.util.Properties

plugins {
    id("updater.android-application")
    id("updater.compose-compiler")
}

val localProperties = providers.fileContents(layout.settingsDirectory.file("local.properties"))
    .asText.orElse("").map { contents ->
        Properties().apply { contents.reader().use { load(it) } }
    }

fun signingProperty(name: String) = localProperties.map { it.getProperty(name) }
    .orElse(providers.environmentVariable(name))

val keystorePath = signingProperty("KEYSTORE_PATH").orNull
val keystorePwd = signingProperty("KEYSTORE_PASS").orNull
val signingAlias = signingProperty("KEY_ALIAS").orNull
val signingPassword = signingProperty("KEY_PASSWORD").orNull

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
}

android {
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePwd
                keyAlias = signingAlias
                keyPassword = signingPassword
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules-android.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    dependenciesInfo.includeInApk = false
    packaging {
        jniLibs {
            excludes += "lib/*/libandroidx.graphics.path.so"
        }
    }
}
