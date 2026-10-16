plugins {
    `kotlin-dsl`
}

dependencies {
    api(project(":common"))
}

gradlePlugin {
    plugins {
        register("versionInfo") {
            id = "updater.version-info"
            implementationClass = "top.yukonga.updater.buildlogic.VersionInfoPlugin"
        }
    }
}
