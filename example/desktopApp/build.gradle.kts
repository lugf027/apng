plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":example:shared"))
            implementation(project(":apng-core"))
            implementation(compose.desktop.currentOs)
            implementation(libs.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
