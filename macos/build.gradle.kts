import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractNativeMacApplicationPackageAppDirTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "main"
            binaryOption("bundleId", ProjectConfig.PACKAGE_NAME)
            binaryOption("smallBinary", "true")
        }
    }

    sourceSets {
        macosMain.dependencies {
            implementation(projects.shared)
        }
    }
}

compose.desktop {
    nativeApplication {
        targets(kotlin.targets.getByName("macosArm64"))
        distributions {
            targetFormats(TargetFormat.Dmg)
            packageName = ProjectConfig.APP_NAME
            packageVersion = ProjectConfig.VERSION_NAME
            description = "Get HyperOS/MIUI recovery ROM info"
            copyright = "Copyright © 2024-2026 YuKongA"
            macOS {
                bundleID = ProjectConfig.PACKAGE_NAME
                iconFile = file("src/macosMain/resources/${ProjectConfig.APP_NAME}.icns")
            }
        }
    }
}

afterEvaluate {
    project.extensions.getByType<KotlinMultiplatformExtension>().targets
        .withType<KotlinNativeTarget>()
        .filter { it.konanTarget == KonanTarget.MACOS_ARM64 }
        .forEach { target ->
            val targetName = target.targetName.uppercaseFirstChar()
            val buildTypes = mapOf(
                NativeBuildType.RELEASE to target.binaries.getExecutable(NativeBuildType.RELEASE),
                NativeBuildType.DEBUG to target.binaries.getExecutable(NativeBuildType.DEBUG)
            )
            buildTypes.forEach { (buildType, executable) ->
                val buildTypeName = buildType.name.lowercase().uppercaseFirstChar()
                target.binaries.withType<Executable>()
                    .filter { it.buildType == buildType }
                    .forEach {
                        val taskName = "copy${buildTypeName}ComposeResourcesFor${targetName}"
                        val copyTask = tasks.register<Copy>(taskName) {
                            from({
                                (executable.compilation.associatedCompilations + executable.compilation).flatMap { compilation ->
                                    compilation.allKotlinSourceSets.map { it.resources }
                                }
                            })
                            into(executable.outputDirectory.resolve("compose-resources"))
                            exclude("*.icns")
                        }
                        it.linkTaskProvider.dependsOn(copyTask)
                    }
            }
        }
}

tasks.withType<AbstractNativeMacApplicationPackageAppDirTask>().configureEach {
    doLast {
        val packageName = packageName.get()
        val destinationDir = outputs.files.singleFile
        val appDir = destinationDir.resolve("$packageName.app")
        val resourcesDir = appDir.resolve("Contents/Resources")
        val currentMacosTarget = kotlin.targets.withType<KotlinNativeTarget>()
            .find { it.konanTarget == KonanTarget.MACOS_ARM64 }?.targetName
        val composeResourcesDir = project.rootDir
            .resolve("macos/build/bin/$currentMacosTarget/releaseExecutable/compose-resources")
        if (composeResourcesDir.exists()) {
            project.copy {
                from(composeResourcesDir)
                into(resourcesDir.resolve("compose-resources"))
            }
        }
    }
}
