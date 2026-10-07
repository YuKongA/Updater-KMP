import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("updater.multiplatform")
}

kotlin {
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()
    macosArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    js {
        browser()
    }
}
