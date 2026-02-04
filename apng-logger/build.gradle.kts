import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmNative") {
                withAndroidTarget()
                withJvm()
                withIos()
            }
            group("skiko") {
                withJvm()
                withIos()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    jvmToolchain(21)

    androidLibrary {
        namespace = "io.github.lugf027.apng.logger"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {}
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ApngLogger"
            isStatic = true
        }
    }

    js(IR) {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // No dependencies needed for logger module
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

@Suppress("UNUSED_VARIABLE")
val kotlinMppExtension: org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension? = null

mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-logger",
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("Kotlin Multiplatform APNG Logger")
        description.set("Logging infrastructure for APNG library with dependency injection support")
        inceptionYear.set("2026")
        url.set("https://github.com/lugf027/apng")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("lugf027")
                name.set("lugf027")
                url.set("https://github.com/lugf027")
            }
        }

        scm {
            url.set("https://github.com/lugf027/apng")
            connection.set("scm:git:https://github.com/lugf027/apng.git")
            developerConnection.set("scm:git:https://github.com/lugf027/apng.git")
        }
    }
}
