@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val appName = "Updater"
val pkgName = "top.yukonga.updater.kmp"
val verName = "1.5.1"
val verCode = getVersionCode()

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    jvmToolchain(21)

    androidTarget()

    jvm("desktop")

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    cocoapods {
        version = verName
        summary = "Get HyperOS/MIUI recovery ROM info"
        homepage = "https://github.com/YuKongA/Updater-KMP"
        authors = "YuKongA"
        license = "AGPL-3.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = appName + "Framework"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            outputModuleName = "updater"
            commonWebpackConfig {
                outputFileName = "updater.js"
            }
        }
        binaries.executable()
    }

    js(IR) {
        browser {
            outputModuleName = "updater"
            commonWebpackConfig {
                outputFileName = "updater.js"
            }
        }
        binaries.executable()
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
            implementation(libs.cryptography.core)
            implementation(libs.image.loader)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.miuix)
            implementation(libs.haze)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.darwin)
        }
        macosMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.darwin)
        }
        jsMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.webcrypto)
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.webcrypto)
            implementation(libs.ktor.client.js)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.cio)
        }
    }
}

android {
    namespace = pkgName
    compileSdk = 36
    defaultConfig {
        applicationId = pkgName
        minSdk = 26
        targetSdk = compileSdk
        versionCode = verCode
        versionName = verName
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules-android.pro")
            androidResources.ignoreAssetsPattern = "icon.png"
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    dependenciesInfo.includeInApk = false
    packaging {
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "$appName-v$versionName($versionCode)-$name.apk"
            }
        }
        resources.excludes += "**"
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"

        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules-jvm.pro")
            version.set("7.6.1")
            optimize.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = appName
            packageVersion = verName
            description = "Get HyperOS/MIUI recovery ROM info"
            copyright = "Copyright © 2024-2025 YuKongA"

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
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    val major = 5
    return major + commitCount
}

val generateVersionInfo by tasks.registering {
    doLast {
        val file = file("src/commonMain/kotlin/misc/VersionInfo.kt")
        file.writeText(
            """
            package misc
            
            object VersionInfo {
                const val VERSION_NAME = "$verName"
                const val VERSION_CODE = $verCode
            }
            """.trimIndent()
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateVersionInfo)
}
