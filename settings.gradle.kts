rootProject.name = "multi-version-catalog-parent"

include("plugin")
findProject(":plugin")?.name = "multi-version-catalog"
