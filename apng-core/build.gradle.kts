plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.okio)
            implementation(libs.atomicfu)
            implementation(libs.coroutines.core)
            implementation(libs.androidx.collection)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
