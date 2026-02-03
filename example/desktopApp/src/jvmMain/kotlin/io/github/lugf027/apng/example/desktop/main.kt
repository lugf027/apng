package io.github.lugf027.apng.example.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import io.github.lugf027.apng.example.App

fun main() {
    singleWindowApplication(
        title = "APNG Demo",
        state = WindowState(
            size = DpSize(1024.dp, 720.dp)
        )
    ) {
        App()
    }
}
