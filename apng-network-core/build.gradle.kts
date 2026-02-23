plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            implementation(compose.runtime)
            implementation(libs.atomicfu)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}
