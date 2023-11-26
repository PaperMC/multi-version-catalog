package io.papermc.multiversioncatalog.util

import org.gradle.api.file.FileCollection
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.file.FileOperations
import org.gradle.initialization.DefaultSettings
import java.io.File
import java.util.function.Function

fun Settings.builtInFileCollectionFactory(): Function<File, FileCollection> {
    // is there a way to do this without using internals?
    // from a settings.gradle.kts we can just do `files(file("path/to/file.toml"))`...
    val settingsInternal = this as DefaultSettings
    val fileOpsInternal = settingsInternal.services[FileOperations::class.java]

    return Function { fileOpsInternal.configurableFiles(it) }
}
