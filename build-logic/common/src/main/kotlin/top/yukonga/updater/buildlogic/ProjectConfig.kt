package top.yukonga.updater.buildlogic

import org.gradle.api.Project
import org.gradle.api.provider.Provider

object ProjectConfig {
    const val JVM_VERSION = 25
    const val APP_NAME = "Updater"
    const val PACKAGE_NAME = "top.yukonga.updater.kmp"
    const val VERSION_NAME = "1.7.0"

    object Android {
        const val TARGET_SDK = 37
        const val MIN_SDK = 26
        const val COMPILE_SDK = 37
        const val COMPILE_SDK_MINOR = 0
        const val BUILD_TOOLS_VERSION = "37.0.0"
    }
}

fun Project.getGitVersionCode(): Provider<Int> = providers.exec {
    workingDir(layout.settingsDirectory)
    commandLine("git", "rev-list", "--count", "HEAD")
}.standardOutput.asText.map { it.trim().toInt() }
