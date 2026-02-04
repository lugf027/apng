package io.github.lugf027.apng.example.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.lugf027.apng.example.App
import io.github.lugf027.apng.example.LoggerConfig
import io.github.lugf027.apng.logger.LogImpl
import io.github.lugf027.apng.logger.LogLevel
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

/**
 * Android-specific log implementation using Logcat.
 */
object AndroidLogImpl : LogImpl {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
            LogLevel.NONE -> { /* no-op */ }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize APNG logger with Android Logcat implementation
        // Change LogLevel to adjust verbosity: VERBOSE, DEBUG, INFO, WARN, ERROR, NONE
        LoggerConfig.init(AndroidLogImpl, LogLevel.DEBUG)
        
        enableEdgeToEdge()
        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    PreviewContextConfigurationEffect()
    App()
}
