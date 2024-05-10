import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

version = "1.0.0"
val pkg = "top.yukonga.updater.kmm"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")

    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // Added
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cryptography.core)
            implementation(libs.ktor.client.core)
            implementation(libs.slf4j.simple)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.darwin)
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
    namespace = pkg
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = pkg
        minSdk = 26
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = version.toString()
    }
    packaging {
        resources {
            excludes += "**"
        }
        applicationVariants.all {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "Updater_KMM-$versionName($versionCode)-$name.apk"
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Updater_KMM"
            packageVersion = "1.0.0"
            macOS {
                iconFile = file("src/desktopMain/resources/macOS/Icon.icns")
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