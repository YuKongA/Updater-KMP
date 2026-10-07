plugins {
    `kotlin-dsl`
}

dependencies {
    api(project(":common"))
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit"))
}

gradlePlugin {
    plugins {
        register("versionInfo") {
            id = "updater.version-info"
            implementationClass = "top.yukonga.updater.buildlogic.VersionInfoPlugin"
        }
    }
}
