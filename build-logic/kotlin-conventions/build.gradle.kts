plugins {
    `kotlin-dsl`
}

dependencies {
    api(project(":common"))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
}
