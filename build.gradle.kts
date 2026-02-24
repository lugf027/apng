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
