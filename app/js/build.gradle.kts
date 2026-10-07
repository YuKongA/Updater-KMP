import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("updater.multiplatform")
    id("updater.compose")
}

kotlin {
    js {
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
        jsMain.dependencies {
            implementation(devNpm("terser-webpack-plugin", libs.versions.terser.webpack.plugin.get()))
        }
    }
}
