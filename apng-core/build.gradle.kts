import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
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
        namespace = "io.github.lugf027"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        withJava() // enable java compilation support
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
            baseName = "ApngCore"
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
            api(project(":apng-logger"))
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.skiko)
            }
        }

        val webMain by getting {
            dependencies {
                implementation(libs.skiko)
            }
        }
    }
}

// For Web/Wasm experimental support
@Suppress("UNUSED_VARIABLE")
val kotlinMppExtension: org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension? = null

mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates(
        groupId = "io.github.lugf027",
        artifactId = "apng-core",
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("Kotlin Multiplatform APNG Core")
        description.set("Core APNG parsing and rendering library for Kotlin Multiplatform")
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
