import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.mavenPublish)
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

kotlin {
    jvm()
}

val _jvmTarget = findProperty("jvmTarget").toString()

subprojects {
    group = findProperty("group") as String
    version = findProperty("version") as String

    if (!name.startsWith("apng")) {
        return@subprojects
    }

    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("com.vanniktech.maven.publish")
    plugins.apply("android-library")

    androidLibrarySetup()
    multiplatformSetup()
    publicationSetup()
}

fun Project.publicationSetup() {
    mavenPublishing {
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
    project.kotlin {

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

        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
            publishLibraryVariants("release")
        }

        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()

        js(IR) {
            browser()
        }

        wasmJs() {
            browser()
        }
    }
}

fun Project.androidLibrarySetup() {
    extensions.configure<LibraryExtension> {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = (findProperty("android.compileSdk") as String).toInt()

        defaultConfig {
            minSdk = (findProperty("android.minSdk") as String).toInt()
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}