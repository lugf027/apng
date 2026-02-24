import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            implementation(libs.compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-resources",
        version = rootProject.version.toString(),
    )

    pom {
        name.set("APNG Resources")
        description.set("Kotlin Multiplatform Resources integration for loading APNG animations from Res")
    }
}
