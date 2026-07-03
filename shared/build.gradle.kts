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
        commonMain.dependencies {
            api(projects.core)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            // Added
            implementation(libs.image.loader)
            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.blur)
            implementation(libs.miuix.preference)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    publicResClass = true
}
