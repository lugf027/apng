import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val jsAppName = project.name + "-js"
val wasmAppName = project.name + "-wasm"

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    js(IR) {
        outputModuleName.set(jsAppName)

        browser {
            commonWebpackConfig {
                outputFileName = "$jsAppName.js"
            }
        }

        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set(wasmAppName)
        browser {
            commonWebpackConfig {
                outputFileName = "$wasmAppName.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(project(":example:shared"))
            implementation(project(":apng-logger"))
        }
    }
}
