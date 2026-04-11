@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val generatedSrcDir = layout.buildDirectory.dir("generated/updater")

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)

    android {
        androidResources.enable = true
        compileSdk {
            version = release(ProjectConfig.Android.COMPILE_SDK) {
                minorApiLevel = ProjectConfig.Android.COMPILE_SDK_MINOR
            }
        }
        minSdk = ProjectConfig.Android.MIN_SDK
        namespace = "${ProjectConfig.PACKAGE_NAME}.shared"
    }

    jvm("desktop")

    fun iosTargets(config: KotlinNativeTarget.() -> Unit) {
        iosArm64(config)
        iosSimulatorArm64(config)
    }
    iosTargets {
        binaries.framework {
            baseName = "shared"
            isStatic = true
            binaryOption("bundleId", ProjectConfig.PACKAGE_NAME)
            binaryOption("smallBinary", "true")
        }
    }

    macosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js(IR) {
        browser()
    }

    sourceSets {
        val desktopMain by getting
        val commonMain by getting {
            kotlin.srcDir(generatedSrcDir.map { it.dir("kotlin") })
        }
        commonMain.dependencies {
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            // Added
            implementation(libs.cryptography.provider.optimal)
            implementation(libs.image.loader)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.blur)
            implementation(libs.miuix.preference)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Added
            implementation(libs.ktor.client.cio)
        }
        appleMain.dependencies {
            // Added
            implementation(libs.ktor.client.darwin)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Added
            implementation(libs.ktor.client.cio)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
        webMain.dependencies {
            // Added
            implementation(libs.ktor.client.js)
        }
    }
}

compose.resources {
    publicResClass = true
}

tasks.named("generateComposeResClass").configure {
    dependsOn(generateVersionInfo)
}

val generateVersionInfo by tasks.registering(GenerateVersionInfoTask::class) {
    versionName.set(ProjectConfig.VERSION_NAME)
    versionCode.set(getGitVersionCode())
    outputFile.set(generatedSrcDir.map { it.file("kotlin/misc/VersionInfo.kt") })
    iosPlistFile.set(layout.projectDirectory.file("../ios/iosApp/Info.plist"))
}
