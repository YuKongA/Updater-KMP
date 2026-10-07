import top.yukonga.updater.buildlogic.ProjectConfig
import top.yukonga.updater.buildlogic.getGitVersionCode

plugins {
    id("com.android.application")
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)
}

val gitVersionCode = getGitVersionCode()

android {
    namespace = ProjectConfig.PACKAGE_NAME
    compileSdk = ProjectConfig.Android.COMPILE_SDK
    buildToolsVersion = ProjectConfig.Android.BUILD_TOOLS_VERSION
    defaultConfig {
        applicationId = ProjectConfig.PACKAGE_NAME
        versionCode = gitVersionCode.get()
        versionName = ProjectConfig.VERSION_NAME
        targetSdk = ProjectConfig.Android.TARGET_SDK
        minSdk = ProjectConfig.Android.MIN_SDK
    }
}

base {
    archivesName.set(gitVersionCode.map {
        "${ProjectConfig.APP_NAME}-v${ProjectConfig.VERSION_NAME}($it)"
    })
}
