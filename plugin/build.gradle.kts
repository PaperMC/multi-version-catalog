import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

val shade: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    implementation(platform(libs.configurate.bom))
    implementation(libs.configurate.core)
    implementation(libs.configurate.toml)
    implementation(libs.gson)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        configurations = listOf(shade)
        fun reloc(pkg: String) = relocate(pkg, "paper.multiversioncatalog.libs.$pkg")
        reloc("io.leangen")
        reloc("me.lucko")
        reloc("org.spongepowered")
        reloc("com.google.gson")
        reloc("com.moandjiezana.toml")
        manifest.attributes("Multi-Release" to true)
    }
    assemble {
        dependsOn(shadowJar)
    }
    withType(KotlinCompile::class).configureEach {
        kotlinOptions.jvmTarget = "1.8"
        // kotlinOptions.freeCompilerArgs += "-Xjdk-release=1.8"
    }
    withType(Jar::class).configureEach {
        from(rootProject.file("LICENSE")) {
            rename("LICENSE", "META-INF/LICENSE_multi-version-catalog")
        }
    }
}

testing {
    suites.named("test", JvmTestSuite::class) {
        useKotlinTest(embeddedKotlinVersion)
        dependencies {
            implementation(libs.junit.api)
            runtimeOnly(libs.junit.engine)
        }
    }
}

publishing.repositories.maven("https://repo.papermc.io/repository/maven-snapshots/") {
    name = "paper"
    credentials(PasswordCredentials::class)
    mavenContent { snapshotsOnly() }
}

gradlePlugin {
    website.set("https://github.com/PaperMC/multi-version-catalog")
    vcsUrl.set("https://github.com/PaperMC/multi-version-catalog")
    plugins.create("plugin") {
        id = "io.papermc.multi-version-catalog"
        displayName = "multi-version-catalog"
        description = "Combines multiple toml files into a single version catalog"
        implementationClass = "io.papermc.multiversioncatalog.MultiVersionCatalogPlugin"
        tags.set(listOf("version-catalogs"))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
