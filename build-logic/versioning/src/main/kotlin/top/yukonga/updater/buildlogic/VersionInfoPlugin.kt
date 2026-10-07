package top.yukonga.updater.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class VersionInfoPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.register<GenerateVersionInfoTask>("generateVersionInfo") {
                description = "Generates Kotlin and Xcode application version information."
                versionName.convention(ProjectConfig.VERSION_NAME)
                versionCode.convention(getGitVersionCode())
                outputDirectory.convention(layout.buildDirectory.dir("generated/updater/kotlin"))
                xcconfigFile.convention(layout.settingsDirectory.file("app/ios/iosApp/Generated.xcconfig"))
            }
        }
    }
}
