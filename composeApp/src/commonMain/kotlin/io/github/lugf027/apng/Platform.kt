package io.github.lugf027.apng

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform