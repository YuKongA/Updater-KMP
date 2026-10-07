package top.yukonga.updater.buildlogic

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionInfoPluginTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `configuration cache tracks Git and build cache restores missing xcconfig`() {
        val project = fixture("original")
        assertEquals(TaskOutcome.SUCCESS, build(project).task(":generateVersionInfo")?.outcome)
        assertVersions(project, 1)

        val repeated = build(project)
        assertEquals(TaskOutcome.UP_TO_DATE, repeated.task(":generateVersionInfo")?.outcome)
        assertTrue(repeated.output.contains("Reusing configuration cache."))

        assertTrue(project.resolve("app/ios/iosApp/Generated.xcconfig").delete())
        assertEquals(TaskOutcome.FROM_CACHE, build(project).task(":generateVersionInfo")?.outcome)
        assertVersions(project, 1)

        commit(project)
        assertEquals(TaskOutcome.SUCCESS, build(project).task(":generateVersionInfo")?.outcome)
        assertVersions(project, 2)
    }

    @Test
    fun `both outputs are restored from cache in a different checkout`() {
        val original = fixture("original")
        assertEquals(TaskOutcome.SUCCESS, build(original).task(":generateVersionInfo")?.outcome)

        val relocated = fixture("relocated")
        assertEquals(TaskOutcome.FROM_CACHE, build(relocated).task(":generateVersionInfo")?.outcome)
        assertVersions(relocated, 1)
    }

    private fun fixture(name: String): File = temporaryFolder.newFolder(name).apply {
        resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "version-info-fixture"
            buildCache {
                local { directory = file("../cache") }
            }
            """.trimIndent(),
        )
        resolve("build.gradle.kts").writeText("plugins { id(\"updater.version-info\") }")
        resolve("gradle.properties").writeText("org.gradle.jvmargs=-Xmx512m -Dfile.encoding=UTF-8\n")
        git(this, "init", "--quiet")
        commit(this)
    }

    private fun build(project: File) = GradleRunner.create()
        .withProjectDir(project)
        .withPluginClasspath()
        .withTestKitDir(temporaryFolder.root.resolve("test-kit"))
        .withArguments(
            "generateVersionInfo", "--parallel", "--build-cache", "--configuration-cache",
            "--configuration-cache-problems=fail", "--max-workers=2", "--stacktrace",
        )
        .build()

    private fun assertVersions(project: File, code: Int) {
        val kotlin = project.resolve("build/generated/updater/kotlin/misc/VersionInfo.kt").readText()
        assertTrue(kotlin.contains("const val VERSION_NAME = \"${ProjectConfig.VERSION_NAME}\""))
        assertTrue(kotlin.contains("const val VERSION_CODE = $code"))
        val xcconfig = project.resolve("app/ios/iosApp/Generated.xcconfig").readText()
        assertTrue(xcconfig.contains("MARKETING_VERSION = ${ProjectConfig.VERSION_NAME}"))
        assertTrue(xcconfig.contains("CURRENT_PROJECT_VERSION = $code"))
    }

    private fun commit(project: File) = git(
        project, "-c", "user.name=Gradle Test", "-c", "user.email=gradle-test@example.invalid",
        "-c", "commit.gpgsign=false", "commit", "--quiet", "--allow-empty", "-m", "Test version",
    )

    private fun git(project: File, vararg args: String) {
        val process = ProcessBuilder(listOf("git", "-C", project.absolutePath) + args)
            .redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        assertEquals(0, process.waitFor(), output)
    }
}
