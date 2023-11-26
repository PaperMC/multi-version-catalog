import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

tasks.shadowJar {
    archiveClassifier = ""
    fun reloc(pkg: String) = relocate(pkg, "paper.multiversioncatalog.libs.$pkg")
    reloc("io.leangen")
    reloc("me.lucko")
    reloc("org.spongepowered")
    reloc("com.google.gson")
    reloc("com.moandjiezana.toml")
}
tasks.assemble {
    dependsOn(tasks.shadowJar)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(embeddedKotlinVersion)
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest(embeddedKotlinVersion)

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) } 
                }
            }
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
    val greeting by plugins.creating {
        id = "io.papermc.multi-version-catalog"
        displayName = "multi-version-catalog"
        description = "Combines multiple toml files into a single version catalog"
        implementationClass = "io.papermc.multiversioncatalog.MultiVersionCatalogPlugin"
        tags.set(listOf("version-catalogs"))
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.check {
    dependsOn(testing.suites.named("functionalTest"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    // kotlinOptions.freeCompilerArgs += "-Xjdk-release=1.8"
}
