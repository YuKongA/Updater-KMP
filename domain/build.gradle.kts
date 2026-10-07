@file:Suppress("UnstableApiUsage")

plugins {
    id("updater.multiplatform-library")
    id("updater.android-library")
    id("updater.serialization")
}

kotlin {
    mingwX64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.protobuf)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
