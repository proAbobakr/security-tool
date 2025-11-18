package com.security.apkanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.security.apkanalyzer.analyzer.SecurityAnalyzer
import com.security.apkanalyzer.domain.AnalysisReport
import com.security.apkanalyzer.instrumentation.RuntimeInstrumentor
import com.security.apkanalyzer.ui.components.*
import com.security.apkanalyzer.ui.theme.ApkAnalyzerTheme
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun MainScreen() {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var analysisReport by remember { mutableStateOf<AnalysisReport?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val instrumentor = remember { RuntimeInstrumentor() }

    val scope = rememberCoroutineScope()
    val analyzer = remember { SecurityAnalyzer() }

    ApkAnalyzerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = { Text("APK Security Analyzer") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            selectedFile = selectFile()
                            if (selectedFile != null) {
                                isAnalyzing = true
                                scope.launch {
                                    try {
                                        analysisReport = analyzer.analyzeApk(selectedFile!!)
                                    } catch (e: Exception) {
                                        println("Error analyzing APK: ${e.message}")
                                    } finally {
                                        isAnalyzing = false
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.FileOpen, "Open File")
                        }
                    }
                )

                if (selectedFile == null) {
                    // Welcome screen
                    WelcomeScreen(
                        onFileSelected = { file ->
                            selectedFile = file
                            isAnalyzing = true
                            scope.launch {
                                try {
                                    analysisReport = analyzer.analyzeApk(file)
                                } catch (e: Exception) {
                                    println("Error analyzing APK: ${e.message}")
                                } finally {
                                    isAnalyzing = false
                                }
                            }
                        }
                    )
                } else {
                    if (isAnalyzing) {
                        // Loading screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Analyzing ${selectedFile?.name}...")
                            }
                        }
                    } else if (analysisReport != null) {
                        // Analysis results
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Tab navigation
                            TabRow(selectedTabIndex = selectedTab) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Overview") },
                                    icon = { Icon(Icons.Default.Dashboard, null) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Malware Detection") },
                                    icon = { Icon(Icons.Default.Security, null) }
                                )
                                Tab(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    text = { Text("OWASP Checks") },
                                    icon = { Icon(Icons.Default.VerifiedUser, null) }
                                )
                                Tab(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    text = { Text("Code Analysis") },
                                    icon = { Icon(Icons.Default.Code, null) }
                                )
                                Tab(
                                    selected = selectedTab == 4,
                                    onClick = { selectedTab = 4 },
                                    text = { Text("Instrumentation") },
                                    icon = { Icon(Icons.Default.Build, null) }
                                )
                            }

                            // Tab content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                when (selectedTab) {
                                    0 -> OverviewTab(analysisReport!!)
                                    1 -> MalwareDetectionTab(analysisReport!!.malwareDetection)
                                    2 -> OwaspChecksTab(analysisReport!!.owaspResults)
                                    3 -> CodeAnalysisTab(analysisReport!!.apkInfo)
                                    4 -> InstrumentationTab(
                                        apkFile = selectedFile!!,
                                        instrumentor = instrumentor
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

@Composable
fun WelcomeScreen(onFileSelected: (File) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "APK Security Analyzer",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Advanced malware detection & OWASP compliance checker",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.width(500.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Features",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FeatureItem(Icons.Default.BugReport, "Comprehensive malware detection")
                    FeatureItem(Icons.Default.VerifiedUser, "OWASP Mobile Top 10 compliance")
                    FeatureItem(Icons.Default.Code, "DEX bytecode analysis")
                    FeatureItem(Icons.Default.Build, "Runtime code instrumentation (Frida-like)")
                    FeatureItem(Icons.Default.Security, "Permission & certificate analysis")
                    FeatureItem(Icons.Default.Assessment, "Security scoring & reporting")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val file = selectFile()
                    if (file != null) {
                        onFileSelected(file)
                    }
                },
                modifier = Modifier.size(width = 300.dp, height = 56.dp)
            ) {
                Icon(Icons.Default.FileOpen, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select APK/AAR File", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                "Supports: APK, AAR files",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

fun selectFile(): File? {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Select APK or AAR file"
    fileChooser.fileFilter = FileNameExtensionFilter(
        "Android Package Files (*.apk, *.aar)",
        "apk",
        "aar"
    )

    val result = fileChooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile
    } else {
        null
    }
}
