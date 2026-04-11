import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "main"
            binaryOption("bundleId", ProjectConfig.PACKAGE_NAME)
            binaryOption("smallBinary", "true")
        }
    }

    sourceSets {
        macosMain.dependencies {
            implementation(projects.shared)
        }
    }
}

compose.desktop {
    nativeApplication {
        targets(kotlin.targets.getByName("macosArm64"))
        distributions {
            targetFormats(TargetFormat.Dmg)
            packageName = ProjectConfig.APP_NAME
            packageVersion = ProjectConfig.VERSION_NAME
            description = "Get HyperOS/MIUI recovery ROM info"
            copyright = "Copyright © 2024-2026 YuKongA"
            macOS {
                bundleID = ProjectConfig.PACKAGE_NAME
                iconFile = file("src/macosMain/resources/${ProjectConfig.APP_NAME}.icns")
            }
        }
    }
}

