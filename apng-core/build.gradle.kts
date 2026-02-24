import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.okio)
            implementation(libs.atomicfu)
            implementation(libs.coroutines.core)
            implementation(libs.androidx.collection)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-core",
        version = rootProject.version.toString(),
    )

    pom {
        name.set("APNG Core")
        description.set("Core APNG parsing, frame composing, and Kotlin Multiplatform animation rendering library")
    }
}
