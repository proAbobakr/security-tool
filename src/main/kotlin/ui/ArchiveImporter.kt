package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.ArchiveExtractor
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun ArchiveImporterApp() {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var archiveInfo by remember { mutableStateOf<ArchiveExtractor.ArchiveInfo?>(null) }
    var extractionResult by remember { mutableStateOf<ArchiveExtractor.ExtractionResult?>(null) }
    var progressMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isExtracting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val extractor = remember { ArchiveExtractor() }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Archive Importer & Analyzer",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Import and extract APK, AAR, AAB, and JAR files",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Select File Button
                Button(
                    onClick = {
                        scope.launch {
                            val file = withContext(Dispatchers.IO) {
                                selectArchiveFile()
                            }
                            file?.let {
                                // Validate that the selected item is a file, not a directory
                                if (it.isDirectory) {
                                    errorMessage = "Cannot select a directory. Please select an archive file (APK, AAR, AAB, or JAR)."
                                    selectedFile = null
                                    archiveInfo = null
                                    extractionResult = null
                                    progressMessages = emptyList()
                                } else {
                                    errorMessage = null
                                    selectedFile = it
                                    archiveInfo = withContext(Dispatchers.IO) {
                                        extractor.getArchiveInfo(it)
                                    }
                                    extractionResult = null
                                    progressMessages = emptyList()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                ) {
                    Text("Select Archive File", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display error message if any
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        backgroundColor = Color(0xFF8B0000),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠ Error",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error,
                                color = Color.White
                            )
                        }
                    }
                }

                // Display selected file info
                selectedFile?.let { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        backgroundColor = Color(0xFF2D2D2D),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Selected File",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Name: ${file.name}", color = Color.LightGray)
                            Text(
                                "Size: ${formatFileSize(file.length())}",
                                color = Color.LightGray
                            )
                            Text("Path: ${file.absolutePath}", color = Color.LightGray)

                            archiveInfo?.let { info ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Entries in archive: ${info.entryCount}",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Extract Button
                    Button(
                        onClick = {
                            scope.launch {
                                isExtracting = true
                                progressMessages = listOf("Starting extraction...")

                                val outputDir = File(file.parentFile, "${file.nameWithoutExtension}_extracted")

                                val result = withContext(Dispatchers.IO) {
                                    extractor.extractArchive(file, outputDir) { message ->
                                        progressMessages = progressMessages + message
                                    }
                                }

                                extractionResult = result
                                isExtracting = false
                            }
                        },
                        enabled = !isExtracting,
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp)
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            if (isExtracting) "Extracting..." else "Extract Archive",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Extraction Result
                extractionResult?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        backgroundColor = if (result.success) Color(0xFF1B5E20) else Color(0xFF8B0000),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                if (result.success) "✓ Extraction Successful" else "✗ Extraction Failed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (result.success) {
                                Text("Files extracted: ${result.filesExtracted}", color = Color.White)
                                Text("Output directory:", color = Color.White)
                                Text(
                                    result.extractedPath ?: "",
                                    color = Color(0xFF81C784),
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    result.errorMessage ?: "Unknown error",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Progress Messages
                if (progressMessages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        backgroundColor = Color(0xFF2D2D2D),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Progress Log",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column {
                                    progressMessages.takeLast(50).forEach { message ->
                                        Text(
                                            "• $message",
                                            color = Color(0xFFB0BEC5),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun selectArchiveFile(): File? {
    val fileChooser = JFileChooser().apply {
        dialogTitle = "Select Archive File"
        fileSelectionMode = JFileChooser.FILES_ONLY

        val filter = FileNameExtensionFilter(
            "Archive Files (*.apk, *.aar, *.aab, *.jar)",
            "apk", "aar", "aab", "jar"
        )
        fileFilter = filter
        isAcceptAllFileFilterUsed = false
    }

    return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile
    } else {
        null
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
