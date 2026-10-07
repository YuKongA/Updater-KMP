@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("updater.multiplatform-library")
    id("updater.android-library")
    id("updater.compose")
    id("updater.serialization")
}

kotlin {
    android {
        androidResources.enable = true
    }

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

    sourceSets {
        val desktopMain = getByName("desktopMain")
        commonMain.dependencies {
            api(projects.data)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            // NPM aggregation needs the BOM in the module declaring versionless Koin dependencies.
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
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
    // Keep the resource API stable when logical Gradle project paths change.
    packageOfResClass = "updater.app.shared.generated.resources"
    publicResClass = true
}
