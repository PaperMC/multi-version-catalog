plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "multi-version-catalog-parent"

include("plugin")
findProject(":plugin")?.name = "multi-version-catalog"
