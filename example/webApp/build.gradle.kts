plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val webMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(project(":example:shared"))
                implementation(compose.foundation)
            }
        }
        val jsMain by getting {
            dependsOn(webMain)
        }
        val wasmJsMain by getting {
            dependsOn(webMain)
        }
    }
}
