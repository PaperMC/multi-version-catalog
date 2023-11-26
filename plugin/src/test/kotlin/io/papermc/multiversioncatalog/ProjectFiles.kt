package io.papermc.multiversioncatalog

import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path

class ProjectFiles(val projectDir: Path) {
    val gradleProperties: Path = resolve("gradle.properties")
    val buildGradle: Path = resolve("build.gradle")
    val buildGradleKts: Path = resolve("build.gradle.kts")
    val settingsGradle: Path = resolve("settings.gradle")
    val settingsGradleKts: Path = resolve("settings.gradle.kts")

    fun resolve(path: String): Path = projectDir.resolve(path)

    fun gradleRunner(): GradleRunner = GradleRunner.create()
        .forwardOutput()
        .withPluginClasspath()
        .withProjectDir(projectDir)
}
