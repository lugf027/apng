import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            baseName = "ApngNetwork"
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
            implementation(project(":apng-network-core"))
            implementation(project(":apng-core"))
            implementation(project(":apng-compose"))
            implementation(libs.ktor.client.core)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val webMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
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
        artifactId = "apng-network",
        version = rootProject.property("VERSION").toString(),
    )

    pom {
        name.set("Kotlin Multiplatform APNG Network")
        description.set("Network loading implementation for APNG with Ktor HTTP client")
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
