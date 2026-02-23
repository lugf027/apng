import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.lugf027.apng.example.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "APNG Example",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        App()
    }
}
