package io.papermc.multiversioncatalog

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class BasicFunctionalTests {

    @Test
    fun `basic project`(@TempDir tempDir: Path) {
        val result = tempDir.copyProject("basic")
            .gradleRunner()
            .withArguments("dependencies")
            .build()

        assertEquals(result.task(":dependencies")?.outcome, TaskOutcome.SUCCESS)

        assertContains(result.output, "org.joml:joml:1.10.5")
        assertContains(result.output, "net.kyori:adventure-text-serializer-ansi:4.14.0")
    }
}
