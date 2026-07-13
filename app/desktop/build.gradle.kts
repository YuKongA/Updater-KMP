import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(projects.app.shared)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"

        // Windows-only: FFM native access + reflective HWND (sun.awt). Gated on build host
        // so non-Windows packages don't warn about the missing sun.awt.windows package.
        if (System.getProperty("os.name").lowercase().contains("win")) {
            jvmArgs += listOf(
                "--enable-native-access=ALL-UNNAMED",
                "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens", "java.desktop/sun.awt.windows=ALL-UNNAMED",
            )
        }

        buildTypes.release.proguard {
            optimize = false
            // Bundled ProGuard maxes at Java 24; pin a newer one for JDK 25 (class major 69).
            version.set("7.9.1")
            configurationFiles.from("proguard-rules-jvm.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = ProjectConfig.APP_NAME
            packageVersion = ProjectConfig.VERSION_NAME
            description = "Get HyperOS/MIUI recovery ROM info"
            copyright = "Copyright © 2024-2026 YuKongA"
            linux {
                iconFile = file("src/desktopMain/resources/linux/Icon.png")
            }
            macOS {
                bundleID = ProjectConfig.PACKAGE_NAME
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
