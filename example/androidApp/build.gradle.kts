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
    implementation(project(":apng-core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
}
