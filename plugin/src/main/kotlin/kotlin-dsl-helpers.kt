import io.papermc.multiversioncatalog.MultiVersionCatalog
import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

fun Settings.multiVersionCatalog(action: Action<MultiVersionCatalog>) {
    extensions.configure(MultiVersionCatalog::class, action)
}

val Settings.multiVersionCatalog: MultiVersionCatalog
    get() = extensions.getByType(MultiVersionCatalog::class)
