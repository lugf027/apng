import androidx.compose.ui.window.ComposeUIViewController
import io.github.lugf027.apng.example.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
