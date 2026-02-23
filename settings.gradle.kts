rootProject.name = "apng"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

include(":apng-core")
include(":apng-network-core")
include(":apng-network")
include(":apng-resources")
include(":example:shared")
include(":example:androidApp")
include(":example:desktopApp")
include(":example:webApp")