import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            implementation(compose.runtime)
            implementation(libs.atomicfu)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-network-core",
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("APNG Network Core")
        description.set("Disk LRU cache and network caching strategy for APNG Compose Multiplatform")
    }
}
