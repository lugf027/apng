plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}
kotlin {
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
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("APNG Resources")
        description.set("Compose Multiplatform Resources integration for loading APNG animations from Res")
    }
}