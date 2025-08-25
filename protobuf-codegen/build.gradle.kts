import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    alias(libs.plugins.google.protobuf)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.0"
    }
    plugins {
        id("pbandk") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:0.16.0:jvm8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("pbandk")
            }
        }
    }
}

tasks.withType<JavaCompile>().all {
    enabled = false
}