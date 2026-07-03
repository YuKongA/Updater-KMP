@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val generatedSrcDir = layout.buildDirectory.dir("generated/updater")

val generateVersionInfo = tasks.register<GenerateVersionInfoTask>("generateVersionInfo") {
    versionName.set(ProjectConfig.VERSION_NAME)
    versionCode.set(getGitVersionCode())
    outputFile.set(generatedSrcDir.map { it.file("kotlin/misc/VersionInfo.kt") })
    xcconfigFile.set(layout.projectDirectory.file("../ios/iosApp/Generated.xcconfig"))
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)

    android {
        compileSdk {
            version = release(ProjectConfig.Android.COMPILE_SDK) {
                minorApiLevel = ProjectConfig.Android.COMPILE_SDK_MINOR
            }
        }
        minSdk = ProjectConfig.Android.MIN_SDK
        namespace = "${ProjectConfig.PACKAGE_NAME}.core"
    }

    jvm("desktop")

    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    mingwX64()
    linuxX64()
    linuxArm64()

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
            kotlin.srcDir(generateVersionInfo.map { generatedSrcDir.get().dir("kotlin") })
        }
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.kotlinx.datetime)
            implementation(libs.cryptography.provider.optimal)
            implementation(libs.okio)
        }
        androidMain.dependencies { implementation(libs.ktor.client.cio) }
        appleMain.dependencies { implementation(libs.ktor.client.darwin) }
        desktopMain.dependencies { implementation(libs.ktor.client.cio) }
        webMain.dependencies { implementation(libs.ktor.client.js) }
        wasmJsMain.dependencies { implementation(libs.kotlinx.browser) }
        mingwMain.dependencies { implementation(libs.ktor.client.winhttp) }
        linuxMain.dependencies { implementation(libs.ktor.client.curl) }
    }
}
