import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("updater.multiplatform")
    id("updater.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = ProjectConfig.APP_NAME
        browser {
            commonWebpackConfig {
                outputFileName = "${ProjectConfig.APP_NAME}.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
        }
        wasmJsMain.dependencies {
            implementation(devNpm("terser-webpack-plugin", libs.versions.terser.webpack.plugin.get()))
        }
    }
}
