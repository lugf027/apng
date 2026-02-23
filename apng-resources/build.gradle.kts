plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            implementation(libs.compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
    }
}
