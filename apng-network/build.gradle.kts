import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            api(project(":apng-network-core"))
            api(libs.okio)
            api(libs.ktor.client.core)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-network",
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("APNG Network")
        description.set("Ktor-based network loading with built-in disk caching for APNG Compose Multiplatform")
    }
}
