plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val jsAppName = project.name + "-js"
val wasmAppName = project.name + "-wasm"

kotlin {
    js(IR) {
        outputModuleName.set(jsAppName)

        browser {
            commonWebpackConfig {
                outputFileName = "$jsAppName.js"
            }
        }

        binaries.executable()
    }

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
        }
    }
}
