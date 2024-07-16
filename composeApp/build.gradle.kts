@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

version = "1.3.0"
val appName = "Updater"
val pkgName = "top.yukonga.updater.kmp"
val xcf = XCFramework(appName + "Framework")

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = appName + "Framework"
            isStatic = true
            freeCompilerArgs += "-Xbinary=bundleId=$pkgName.framework"
            xcf.add(this)
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // Added
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cryptography.core)
            implementation(libs.ktor.client.core)
            implementation(libs.image.loader)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.hiddenapibypass)
        }
        iosMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.ios)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.okhttp)
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = pkgName
    compileSdk = 35
    defaultConfig {
        applicationId = pkgName
        minSdk = 26
        targetSdk = 35
        versionCode = getVersionCode()
        versionName = version.toString()
    }
    val properties = Properties()
    runCatching { properties.load(project.rootProject.file("local.properties").inputStream()) }
    val keystorePath = properties.getProperty("KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH")
    val keystorePwd = properties.getProperty("KEYSTORE_PASS") ?: System.getenv("KEYSTORE_PASS")
    val alias = properties.getProperty("KEY_ALIAS") ?: System.getenv("KEY_ALIAS")
    val pwd = properties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD")
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePwd
                keyAlias = alias
                keyPassword = pwd
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles("proguard-rules.pro")
            androidResources.ignoreAssetsPattern = "icon.png"
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
            applicationIdSuffix = ".debug"
        }
    }
    dependenciesInfo.includeInApk = false
    packaging {
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "$appName-$versionName($versionCode)-$name.apk"
            }
        }
        resources.excludes += "**"
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = appName
            packageVersion = version.toString()
            description = "Get HyperOS/MIUI recovery ROM update info"
            copyright = "Copyright © 2024 YuKongA, AkaneTan"

            linux {
                iconFile = file("src/desktopMain/resources/linux/Icon.png")
            }
            macOS {
                bundleID = pkgName
                iconFile = file("src/desktopMain/resources/macos/Icon.icns")
            }
            windows {
                dirChooser = true
                perUserInstall = true
                iconFile = file("src/desktopMain/resources/windows/Icon.ico")
            }
        }
    }
}

fun getGitCommitCount(): Int {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = out
    }
    return out.toString().trim().toInt()
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    val major = 5
    return major + commitCount
}