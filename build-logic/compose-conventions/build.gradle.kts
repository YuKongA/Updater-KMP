plugins {
    `kotlin-dsl`
}

dependencies {
    api(project(":common"))
    implementation(libs.compose.gradle.plugin)
    implementation(libs.kotlin.compose.compiler.gradle.plugin)
}
