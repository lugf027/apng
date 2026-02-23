plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
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
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("APNG Core")
        description.set("Core APNG parsing, frame composing, and Compose Multiplatform animation rendering library")
    }
}
