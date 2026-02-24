package io.github.lugf027.apng.example

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op: non-Android platforms don't have system back gestures
}
