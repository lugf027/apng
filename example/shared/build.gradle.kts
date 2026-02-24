import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.file.DirectoryProperty
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

/**
 * Workaround for JetBrains Compose Resources + com.android.kotlin.multiplatform.library (AGP 8.10+).
 *
 * The Compose Resources plugin calls `variant.sources.assets?.addGeneratedSourceDirectory(...)` to
 * register compose resources as Android assets. However, with the KMP Android library plugin,
 * `sources.assets` returns null, so `CopyResourcesToAndroidAssetsTask.outputDirectory` is never
 * configured and resources are not included in the AAR/APK.
 *
 * This sets a fallback outputDirectory so the copy task doesn't fail.
 * The consuming app module (androidApp) must also register this output as an assets source.
 */
tasks.configureEach {
    if (name.contains("ComposeResourcesToAndroidAssets")) {
        val outProp = this::class.java.methods.firstOrNull { it.name == "getOutputDirectory" }
        if (outProp != null) {
            val dirProp = outProp.invoke(this) as? DirectoryProperty
            if (dirProp != null && !dirProp.isPresent) {
                dirProp.set(project.layout.buildDirectory.dir("generated/compose/androidAssets/$name"))
            }
        }
    }
}
