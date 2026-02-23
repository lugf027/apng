kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apng-core"))
            api(project(":apng-network-core"))
            api(libs.okio)
            api(libs.ktor.client.core)
        }
    }
}
