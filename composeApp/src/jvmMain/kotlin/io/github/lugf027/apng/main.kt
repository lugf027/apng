package io.github.lugf027.apng

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "apng",
    ) {
        App()
    }
}