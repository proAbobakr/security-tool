import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.ArchiveImporterApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Security Tool - Archive Importer",
        state = rememberWindowState(width = 900.dp, height = 800.dp)
    ) {
        ArchiveImporterApp()
    }
}
