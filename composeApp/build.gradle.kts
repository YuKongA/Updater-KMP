@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractNativeMacApplicationPackageAppDirTask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hotReload)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val appName = "Updater"
val pkgName = "top.yukonga.updater.kmp"
val verName = "1.6.0"
val verCode = getVersionCode()
val generatedSrcDir = layout.buildDirectory.dir("generated").get().asFile.resolve("updater")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    jvmToolchain(21)

    androidTarget()

    jvm("desktop")

    fun iosTargets(config: KotlinNativeTarget.() -> Unit) {
        iosX64(config)
        iosArm64(config)
        iosSimulatorArm64(config)
    }
    iosTargets {
        binaries.framework {
            baseName = "shared"
            isStatic = true
        }
        compilerOptions {
            freeCompilerArgs.add("-Xbinary=preCodegenInlineThreshold=40")
        }
    }

    fun macosTargets(config: KotlinNativeTarget.() -> Unit) {
        macosX64(config)
        macosArm64(config)
    }
    macosTargets {
        binaries.executable {
            entryPoint = "main"
        }
        compilerOptions {
            freeCompilerArgs.add("-Xbinary=preCodegenInlineThreshold=40")
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "updater"
        browser {
            commonWebpackConfig {
                outputFileName = "updater.js"
            }
        }
        binaries.executable()
    }

    js(IR) {
        outputModuleName = "updater"
        browser {
            commonWebpackConfig {
                outputFileName = "updater.js"
            }
        }
        compilerOptions {
            freeCompilerArgs.add("-Xes-long-as-bigint")
            freeCompilerArgs.add("-XXLanguage:+JsAllowLongInExportedDeclarations")
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        val commonMain by getting {
            kotlin.srcDir(generatedSrcDir.resolve("kotlin").absolutePath)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
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
        appleMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.apple)
            implementation(libs.ktor.client.darwin)
        }
        webMain.dependencies {
            // Added
            implementation(libs.cryptography.provider.webcrypto)
            implementation(libs.ktor.client.js)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Added
            implementation(libs.cryptography.provider.jdk)
            implementation(libs.ktor.client.cio)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
    }
}

android {
    namespace = pkgName
    defaultConfig {
        applicationId = pkgName
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
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("**")
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"

        buildTypes.release.proguard {
            optimize = false
            configurationFiles.from("proguard-rules-jvm.pro")
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
                jvmArgs("-Dapple.awt.application.appearance=system")
                iconFile = file("src/desktopMain/resources/macos/Icon.icns")
            }
            windows {
                dirChooser = true
                perUserInstall = true
                iconFile = file("src/desktopMain/resources/windows/Icon.ico")
            }
        }
    }
    nativeApplication {
        targets(kotlin.targets.getByName("macosArm64"), kotlin.targets.getByName("macosX64"))
        distributions {
            targetFormats(TargetFormat.Dmg)
            packageName = appName
            packageVersion = verName
            description = "Get HyperOS/MIUI recovery ROM info"
            copyright = "Copyright © 2024-2025 YuKongA"
            macOS {
                bundleID = pkgName
                iconFile = file("src/macosMain/resources/Updater.icns")
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
        val file = generatedSrcDir.resolve("kotlin/misc/VersionInfo.kt")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(
            """
            package misc

            object VersionInfo {
                const val VERSION_NAME = "$verName"
                const val VERSION_CODE = $verCode
            }
            """.trimIndent()
        )
        val iosPlist = project.rootDir.resolve("iosApp/iosApp/Info.plist")
        if (iosPlist.exists()) {
            val content = iosPlist.readText()
            val updatedContent = content
                .replace(
                    Regex("<key>CFBundleShortVersionString</key>\\s*<string>[^<]*</string>"),
                    "<key>CFBundleShortVersionString</key>\n\t<string>$verName</string>"
                )
                .replace(
                    Regex("<key>CFBundleVersion</key>\\s*<string>[^<]*</string>"),
                    "<key>CFBundleVersion</key>\n\t<string>$verCode</string>"
                )
            iosPlist.writeText(updatedContent)
        }
    }
}

tasks.named("generateComposeResClass").configure {
    dependsOn(generateVersionInfo)
}

afterEvaluate {
    project.extensions.getByType<KotlinMultiplatformExtension>().targets
        .withType<KotlinNativeTarget>()
        .filter { it.konanTarget == KonanTarget.MACOS_ARM64 || it.konanTarget == KonanTarget.MACOS_X64 }
        .forEach { target ->
            val targetName = target.targetName.uppercaseFirstChar()
            val buildTypes = mapOf(
                NativeBuildType.RELEASE to target.binaries.getExecutable(NativeBuildType.RELEASE),
                NativeBuildType.DEBUG to target.binaries.getExecutable(NativeBuildType.DEBUG)
            )
            buildTypes.forEach { (buildType, executable) ->
                val buildTypeName = buildType.name.lowercase().uppercaseFirstChar()
                target.binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Executable>()
                    .filter { it.buildType == buildType }
                    .forEach {
                        val taskName = "copy${buildTypeName}ComposeResourcesFor${targetName}"
                        val copyTask = tasks.register<Copy>(taskName) {
                            from({
                                (executable.compilation.associatedCompilations + executable.compilation).flatMap { compilation ->
                                    compilation.allKotlinSourceSets.map { it -> it.resources }
                                }
                            })
                            into(executable.outputDirectory.resolve("compose-resources"))
                            exclude("*.icns")
                        }
                        it.linkTaskProvider.dependsOn(copyTask)
                    }
            }
        }
}

tasks.withType<AbstractNativeMacApplicationPackageAppDirTask>().configureEach {
    doLast {
        val packageName = packageName.get()
        val destinationDir = outputs.files.singleFile
        val appDir = destinationDir.resolve("$packageName.app")
        val resourcesDir = appDir.resolve("Contents/Resources")
        val currentMacosTarget = kotlin.targets.withType<KotlinNativeTarget>()
            .find { it.konanTarget == KonanTarget.MACOS_ARM64 || it.konanTarget == KonanTarget.MACOS_X64 }?.targetName
        val composeResourcesDir = project.rootDir
            .resolve("composeApp/build/bin/$currentMacosTarget/releaseExecutable/compose-resources")
        if (composeResourcesDir.exists()) {
            project.copy {
                from(composeResourcesDir)
                into(resourcesDir.resolve("compose-resources"))
            }
        }
    }
}
