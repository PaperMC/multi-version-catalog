package io.papermc.multiversioncatalog

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

abstract class MultiVersionCatalogPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.extensions.create("multiVersionCatalog", MultiVersionCatalog::class, target)
    }
}
