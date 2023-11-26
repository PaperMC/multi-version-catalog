package io.papermc.multiversioncatalog

import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

fun GradleRunner.withProjectDir(dir: Path): GradleRunner = withProjectDir(dir.toFile())

@OptIn(ExperimentalPathApi::class)
fun Path.copyProject(resourcesProjectName: String): ProjectFiles {
    Paths.get("src/test/resources/projects/$resourcesProjectName")
        .copyToRecursively(this, followLinks = false)
    return ProjectFiles(this)
}
