package com.security.apkanalyzer

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.security.apkanalyzer.ui.MainScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "APK Security Analyzer - Malware Detection & OWASP Checker",
        state = rememberWindowState(width = 1400.dp, height = 900.dp)
    ) {
        MainScreen()
    }
}
