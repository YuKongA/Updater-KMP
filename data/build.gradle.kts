@file:Suppress("UnstableApiUsage")

import top.yukonga.updater.buildlogic.GenerateVersionInfoTask

plugins {
    id("updater.multiplatform-library")
    id("updater.android-library")
    id("updater.serialization")
    id("updater.version-info")
}

val generateVersionInfo = tasks.named<GenerateVersionInfoTask>("generateVersionInfo")

kotlin {
    mingwX64()
    linuxX64()

    sourceSets {
        val desktopMain = getByName("desktopMain")
        named("commonMain") {
            kotlin.srcDir(generateVersionInfo.flatMap { it.outputDirectory })
        }
        commonMain.dependencies {
            api(projects.domain)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.ktor.client.core)
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.okio)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        androidMain.dependencies { implementation(libs.ktor.client.cio) }
        appleMain.dependencies { implementation(libs.ktor.client.darwin) }
        desktopMain.dependencies { implementation(libs.ktor.client.cio) }
        webMain.dependencies { implementation(libs.ktor.client.js) }
        wasmJsMain.dependencies { implementation(libs.kotlinx.browser) }
        mingwMain.dependencies { implementation(libs.ktor.client.winhttp) }
        linuxMain.dependencies { implementation(libs.ktor.client.curl) }
    }
}
