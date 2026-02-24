import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

val _jvmTarget = findProperty("jvmTarget").toString()

android {
    namespace = "io.github.lugf027.apng.example.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "io.github.lugf027.apng.example.android"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
    }
}

dependencies {
    implementation(project(":example:shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
}

/**
 * Workaround: com.android.kotlin.multiplatform.library (used by :example:shared) does not support
 * Android assets in its variant Sources API (`sources.assets` returns null). The JetBrains Compose
 * Resources plugin's CopyResourcesToAndroidAssetsTask fails to register its output, so compose
 * resources from KMP library modules are not included in the APK.
 *
 * This workaround registers the shared module's compose resources output directory as an Android
 * assets source in the consuming app module, and wires task dependencies so resources are copied
 * before being merged into the APK.
 */
val sharedComposeAssetsDir = project(":example:shared").layout.buildDirectory.dir(
    "generated/compose/androidAssets/copyAndroidMainComposeResourcesToAndroidAssets"
)

android.sourceSets.getByName("main").assets.srcDir(sharedComposeAssetsDir)

tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Assets")) {
        dependsOn(":example:shared:copyAndroidMainComposeResourcesToAndroidAssets")
    }
}
