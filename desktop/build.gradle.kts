import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
}

val appName = "Updater"
val pkgName = "top.yukonga.updater.kmp"
val verName = "1.6.1"

kotlin {
    jvmToolchain(21)

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.desktop.currentOs)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
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
}
