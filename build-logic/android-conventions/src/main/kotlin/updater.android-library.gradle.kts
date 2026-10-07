@file:Suppress("UnstableApiUsage")

import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("updater.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        compileSdk {
            version = release(ProjectConfig.Android.COMPILE_SDK) {
                minorApiLevel = ProjectConfig.Android.COMPILE_SDK_MINOR
            }
        }
        minSdk = ProjectConfig.Android.MIN_SDK
        namespace = "${ProjectConfig.PACKAGE_NAME}.${project.name}"
    }
}
