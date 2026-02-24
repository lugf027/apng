import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.mavenPublish).apply(false)
}

rootProject.projectDir.resolve("local.properties").let {
    if (it.exists()) {
        Properties().apply {
            load(FileInputStream(it))
        }.forEach { (k, v) -> rootProject.ext.set(k.toString(), v) }
        System.getenv().forEach { (k, v) ->
            rootProject.ext.set(k, v)
        }
    }
}

val _jvmTarget = findProperty("jvmTarget").toString()

// Resolve version: prioritize VERSION_TAG env var (set by CI from git tag), fall back to gradle.properties
val resolvedVersion: String = System.getenv("VERSION_TAG").takeUnless { it.isNullOrBlank() }
    ?: findProperty("VERSION") as String

version = resolvedVersion

subprojects {
    group = findProperty("group") as String
    version = resolvedVersion

    if (!name.startsWith("apng")) {
        return@subprojects
    }

    plugins.apply("com.android.kotlin.multiplatform.library")
    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("com.vanniktech.maven.publish")

    multiplatformSetup()
    publicationSetup()
}

fun Project.publicationSetup() {
    extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(true)
        signAllPublications()

        pom {
            url.set("https://github.com/lugf027/apng")
            inceptionYear.set("2026")

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
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.multiplatformSetup() {
    extensions.configure<KotlinMultiplatformExtension> {

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

        jvm("desktop") {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
        }

        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()

        js(IR) {
            browser()
        }

        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }

        // The com.android.kotlin.multiplatform.library plugin creates the android target,
        // but the hierarchy template doesn't automatically connect it to custom groups.
        // Manually wire androidMain into the same intermediate source sets as other JVM targets.
        sourceSets.named("androidMain").configure {
            dependsOn(sourceSets.getByName("jvmNativeMain"))
            dependsOn(sourceSets.getByName("javaMain"))
        }
    }

    // Configure Android target via the KMP Android library plugin
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
            namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
            compileSdk = (findProperty("android.compileSdk") as String).toInt()
            minSdk = (findProperty("android.minSdk") as String).toInt()
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
        }
    }

}

// ── Compose Resources + KMP Android Library Workaround ──────────────────────
//
// JetBrains Compose Resources plugin (1.10.x) calls
//   variant.sources.assets?.addGeneratedSourceDirectory(...)
// to register compose resources as Android assets. With com.android.kotlin.multiplatform.library
// (AGP 8.10+), `sources.assets` returns null, so CopyResourcesToAndroidAssetsTask's
// `outputDirectory` is never configured and resources are lost.
//
// Two-part fix:
//   1. fixComposeResourcesForKmpAndroid() — called by the KMP library that owns composeResources.
//      Sets a fallback outputDirectory so the copy task succeeds.
//   2. consumeKmpLibraryComposeAssets() — called by the Android app that depends on that library.
//      Registers the library's compose resources output as an Android assets source.
//
// These are only needed for modules using com.android.kotlin.multiplatform.library that contain
// (or consume) Compose Resources. The apng-* library modules have no composeResources and do not
// need this workaround.

/**
 * Fix for KMP library modules that own composeResources:
 * Sets a fallback `outputDirectory` on CopyResourcesToAndroidAssetsTask when the Compose Resources
 * plugin fails to configure it (due to `variant.sources.assets` being null).
 */
fun Project.fixComposeResourcesForKmpAndroid() {
    tasks.configureEach {
        if (name.contains("ComposeResourcesToAndroidAssets")) {
            val outProp = this::class.java.methods.firstOrNull { it.name == "getOutputDirectory" }
            if (outProp != null) {
                val dirProp = outProp.invoke(this) as? org.gradle.api.file.DirectoryProperty
                if (dirProp != null && !dirProp.isPresent) {
                    dirProp.set(project.layout.buildDirectory.dir("generated/compose/androidAssets/$name"))
                }
            }
        }
    }
}

/**
 * Fix for Android app modules that consume a KMP library with composeResources:
 * Registers the library's compose resources output directory as an Android assets source and
 * wires task dependencies so resources are copied before being merged into the APK.
 *
 * @param libraryProjectPath Gradle project path of the KMP library (e.g. ":example:shared")
 */
fun Project.consumeKmpLibraryComposeAssets(libraryProjectPath: String) {
    val copyTaskName = "copyAndroidMainComposeResourcesToAndroidAssets"
    val assetsDir = project(libraryProjectPath).layout.buildDirectory.dir(
        "generated/compose/androidAssets/$copyTaskName"
    )

    extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
        sourceSets.getByName("main").assets.srcDir(assetsDir)
    }

    tasks.configureEach {
        if (name.startsWith("merge") && name.endsWith("Assets")) {
            dependsOn("$libraryProjectPath:$copyTaskName")
        }
    }
}

// Expose workaround functions to subproject build scripts via extra properties.
// Subprojects call: val fn by rootProject.extra; fn.invoke(this)
rootProject.extra["fixComposeResourcesForKmpAndroid"] = Action<Project> { fixComposeResourcesForKmpAndroid() }
rootProject.extra["consumeKmpLibraryComposeAssets"] =
    { proj: Project, libraryPath: String -> proj.consumeKmpLibraryComposeAssets(libraryPath) }
