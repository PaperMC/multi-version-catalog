import io.papermc.multiversioncatalog.MultiVersionCatalogs
import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.internal.file.FileOperations
import org.gradle.initialization.DefaultSettings
import org.gradle.kotlin.dsl.*

fun Settings.multiVersionCatalogs(action: Action<MultiVersionCatalogs>) {
    extensions.configure(MultiVersionCatalogs::class, action)
}

val Settings.multiVersionCatalogs: MultiVersionCatalogs
    get() = extensions.getByType(MultiVersionCatalogs::class)

fun Settings.loadVersionCatalogs(
    catalogName: String,
    vararg catalogs: Any
): VersionCatalogBuilder {
    return loadVersionCatalogs(catalogName, catalogs.toList())
}

fun Settings.loadVersionCatalogs(
    catalogName: String,
    catalogs: Collection<Any>
): VersionCatalogBuilder {
    // is there a way to do this without using internals?
    // from a settings.gradle.kts we can just do `files(file("path/to/file.toml"))`...
    val settingsInternal = this as DefaultSettings
    val fileOpsInternal = settingsInternal.services[FileOperations::class.java]

    return multiVersionCatalogs.loadVersionCatalogs(catalogName, catalogs) {
        fileOpsInternal.configurableFiles(it)
    }
}
