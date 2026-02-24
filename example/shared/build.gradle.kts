import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

val _jvmTarget = findProperty("jvmTarget").toString()

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmNative") {
                withAndroidTarget()
                withJvm()
                withIos()
                withMacos()
            }
            group("java") {
                withJvm()
                withAndroidTarget()
            }
            group("skiko") {
                withJvm()
                withIos()
                withMacos()
                withJs()
                withWasmJs()
            }
            group("desktopNative") {
                withJvm()
                withIos()
                withMacos()
            }
        }
    }

    jvm("desktop")
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    macosArm64()
    macosX64()
    js(IR) { browser() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    // The com.android.kotlin.multiplatform.library plugin creates the android target,
    // but the hierarchy template doesn't automatically connect it to custom groups.
    // Manually wire androidMain into the same intermediate source sets as other JVM targets.
    sourceSets.named("androidMain").configure {
        dependsOn(sourceSets.getByName("jvmNativeMain"))
        dependsOn(sourceSets.getByName("javaMain"))
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.apng.core)
            implementation(libs.apng.network)
            implementation(libs.apng.resources)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }

    // Configure Android target via the KMP Android library plugin
    targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
        namespace = "io.github.lugf027.apng.example.shared"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
        }
    }
}

// Workaround: Compose Resources + KMP Android library — see root build.gradle.kts for details.
@Suppress("UNCHECKED_CAST")
(rootProject.extra["fixComposeResourcesForKmpAndroid"] as Action<Project>).execute(project)
