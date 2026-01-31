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

val generatedSrcDir = layout.buildDirectory.dir("generated").get().asFile.resolve("updater")

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)

    android {
        androidResources.enable = true
        compileSdk = ProjectConfig.Android.COMPILE_SDK
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
            kotlin.srcDir(generatedSrcDir.resolve("kotlin").absolutePath)
        }
        commonMain.dependencies {
            api(libs.ui)
            api(libs.components.resources)
            // Added
            implementation(libs.cryptography.core)
            implementation(libs.image.loader)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.miuix)
            implementation(libs.miuix.icons)
            implementation(libs.haze)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.cio)
        }
        appleMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.darwin)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.cio)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
        webMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.webcrypto)
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

val generateVersionInfo by tasks.registering {
    doLast {
        val file = generatedSrcDir.resolve("kotlin/misc/VersionInfo.kt")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(
            """
            package misc

            object VersionInfo {
                const val VERSION_NAME = "${ProjectConfig.VERSION_NAME}"
                const val VERSION_CODE = ${ProjectConfig.VERSION_CODE}
            }
            """.trimIndent()
        )
        val iosPlist = project.rootDir.resolve("ios/iosApp/Info.plist")
        if (iosPlist.exists()) {
            val content = iosPlist.readText()
            val updatedContent = content
                .replace(
                    Regex("<key>CFBundleShortVersionString</key>\\s*<string>[^<]*</string>"),
                    "<key>CFBundleShortVersionString</key>\n\t<string>${ProjectConfig.VERSION_NAME}</string>"
                )
                .replace(
                    Regex("<key>CFBundleVersion</key>\\s*<string>[^<]*</string>"),
                    "<key>CFBundleVersion</key>\n\t<string>${ProjectConfig.VERSION_CODE}</string>"
                )
            iosPlist.writeText(updatedContent)
        }
    }
}
