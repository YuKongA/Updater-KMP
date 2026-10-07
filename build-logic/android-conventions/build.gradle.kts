plugins {
    `kotlin-dsl`
}

dependencies {
    api(project(":kotlin-conventions"))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
}
