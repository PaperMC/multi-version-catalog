package io.papermc.multiversioncatalog

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.*

abstract class MultiVersionCatalogsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.extensions.create("multiVersionCatalog", MultiVersionCatalogs::class, target)
    }
}
