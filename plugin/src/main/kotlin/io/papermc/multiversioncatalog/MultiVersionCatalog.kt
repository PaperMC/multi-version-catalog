package io.papermc.multiversioncatalog

import io.papermc.multiversioncatalog.util.builtInFileCollectionFactory
import me.lucko.configurate.toml.TOMLConfigurationLoader
import org.gradle.api.file.FileCollection
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.spongepowered.configurate.ConfigurationNode
import java.io.File
import java.nio.file.Path
import java.util.function.Function
import javax.inject.Inject
import kotlin.io.path.exists

abstract class MultiVersionCatalog @Inject constructor(
    private val settings: Settings,
) {
    @JvmOverloads
    fun fromFiles(
        catalogName: String,
        files: Collection<Any>,
        fileCollectionFactory: Function<File, FileCollection> = settings.builtInFileCollectionFactory()
    ): VersionCatalogBuilder {
        return fromStrings(
            catalogName,
            files.mapNotNull(::fileText),
            fileCollectionFactory
        )
    }

    private fun fileText(file: Any) = when (file) {
        is File -> file.takeIf { it.exists() }?.readText()
        is Path -> file.takeIf { it.exists() }?.toFile()?.readText()
        is String -> settings.rootDir.resolve(file).takeIf { f -> f.exists() }?.readText()
        else -> error("Cannot load version catalog from $file (type: ${file::class.java})")
    }

    @JvmOverloads
    fun fromFiles(
        catalogName: String,
        vararg files: Any,
        fileCollectionFactory: Function<File, FileCollection> = settings.builtInFileCollectionFactory()
    ): VersionCatalogBuilder {
        return fromFiles(catalogName, files.toList(), fileCollectionFactory)
    }

    @JvmOverloads
    fun fromStrings(
        catalogName: String,
        docs: List<String>,
        fileCollectionFactory: Function<File, FileCollection> = settings.builtInFileCollectionFactory()
    ): VersionCatalogBuilder {
        val merged = settings.rootDir.resolve(mergedLocation(catalogName))
        merged.parentFile.mkdirs()
        if (docs.isEmpty()) {
            return loadCatalogFile(catalogName, null, fileCollectionFactory)
        } else if (docs.size == 1) {
            merged.writeText(docs.single())
            return loadCatalogFile(catalogName, merged, fileCollectionFactory)
        } else {
            val rootNodes = docs.map { doc ->
                TOMLConfigurationLoader.builder().buildAndLoadString(
                    doc.replace(VERSION_REF, VERSION_REF_HACK)
                )
            }

            val node = rootNodes.first()

            rootNodes.asSequence().drop(1).forEach { documentRoot ->
                node.mergeFrom(documentRoot)
            }

            // val catalogText = printVersionCatalogToml(node)
            val catalogText = TOMLConfigurationLoader.builder()
                .apply {
                    keyIndent = 2
                }
                .buildAndSaveString(node)
                .replace(VERSION_REF_HACK, VERSION_REF)

            if (!merged.exists() || merged.readText() != catalogText) {
                merged.writeText(catalogText)
            }

            return loadCatalogFile(catalogName, merged, fileCollectionFactory)
        }
    }

    private fun loadCatalogFile(
        name: String,
        file: File?,
        fileCollectionFactory: Function<File, FileCollection>
    ): VersionCatalogBuilder {
        return settings.dependencyResolutionManagement.versionCatalogs.create(name) {
            file?.let { from(fileCollectionFactory.apply(it)) }
        }
    }

    @Suppress("unused")
    private fun printVersionCatalogToml(node: ConfigurationNode): String {
        val output = StringBuilder()
        for (s in TOP_LEVEL) {
            node.node(s).takeIf { !it.virtual() && it.isMap }?.let { section ->
                output.append("[$s]\n")
                section.childrenMap().forEach { (key, node) ->
                    output.append("$key = ")
                    if (node.rawScalar() is String) {
                        output.append("\"${node.rawScalar()}\"\n")
                    } else if (node.isMap) {
                        output.append("{ ")
                        val children = node.childrenMap().entries.toList()
                        children.forEachIndexed { index, (childKey, childNode) ->
                            output.append(if (childKey == VERSION_REF_HACK) VERSION_REF else childKey)
                            val value = childNode.rawScalar()
                            output.append(" = \"$value\"")
                            if (children.lastIndex != index) {
                                output.append(", ")
                            }
                        }
                        output.append(" }\n")
                    } else if (node.isList) {
                        output.append('[')
                        val children = node.childrenList()
                        children.map { it.rawScalar() }.forEachIndexed { index, raw ->
                            output.append("\"$raw\"")
                            if (children.lastIndex != index) {
                                output.append(", ")
                            }
                        }
                        output.append("]\n")
                    }
                }
            }
        }
        return output.toString()
    }

    private companion object {
        const val VERSION_REF = "version.ref"
        const val VERSION_REF_HACK = "__versionref__"
        val TOP_LEVEL = listOf("versions", "plugins", "libraries", "bundles")

        fun mergedLocation(name: String) = ".gradle/caches/multi-version-catalog/$name-merged.versions.toml"
    }
}
