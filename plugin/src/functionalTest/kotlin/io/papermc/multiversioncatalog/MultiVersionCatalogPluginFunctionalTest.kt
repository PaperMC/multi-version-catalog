package io.papermc.multiversioncatalog

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MultiVersionCatalogPluginFunctionalTest {

    private val File.buildFile get() = resolve("build.gradle")
    private val File.settingsFile get() = resolve("settings.gradle")

    @Test fun `dependencies task runs`(@TempDir projectDir: File) {
        projectDir.resolve("api.versions.toml").writeText("""
            [versions]
            adventure = "4.14.0"

            [libraries]
            joml = "org.joml:joml:1.10.5"
        """.trimIndent())
        projectDir.resolve("server.versions.toml").writeText("""
            [libraries]
            adventure-text-serializer-ansi = { module = "net.kyori:adventure-text-serializer-ansi", version.ref = "adventure" }
        """.trimIndent())
        projectDir.settingsFile.writeText("""
            plugins {
                id("io.papermc.multi-version-catalog")
            }
            
            def mvc = extensions.getByType(io.papermc.multiversioncatalog.MultiVersionCatalog)
            mvc.fromFiles("libs", "server.versions.toml", "api.versions.toml")
        """.trimIndent())
        projectDir.buildFile.writeText("""
            plugins {
                id 'java-library'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                api libs.joml
                api libs.adventure.text.serializer.ansi
            }
        """.trimIndent())

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("dependencies")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        assertEquals(result.task(":dependencies")?.outcome, TaskOutcome.SUCCESS)
        assertContains(result.output, "org.joml:joml:1.10.5")
        assertContains(result.output, "net.kyori:adventure-text-serializer-ansi:4.14.0")
    }
}
