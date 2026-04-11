object ProjectConfig {
    const val JVM_VERSION = 21
    const val APP_NAME = "Updater"
    const val PACKAGE_NAME = "top.yukonga.updater.kmp"
    const val VERSION_NAME = "1.6.2"

    object Android {
        const val TARGET_SDK = 37
        const val MIN_SDK = 26
        const val COMPILE_SDK = 37
        const val COMPILE_SDK_MINOR = 0
        const val BUILD_TOOLS_VERSION = "37.0.0"
    }
}

fun org.gradle.api.Project.getGitVersionCode(): Int {
    return providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()
}
