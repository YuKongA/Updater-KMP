import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("updater.multiplatform")
    id("updater.compose")
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
