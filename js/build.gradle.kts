plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js(IR) {
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
            implementation(project(":shared"))
        }
    }
}
