package com.security.apkanalyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.security.apkanalyzer.domain.HookType
import com.security.apkanalyzer.domain.InstrumentationHook
import com.security.apkanalyzer.instrumentation.RuntimeInstrumentor
import java.io.File

@Composable
fun InstrumentationTab(
    apkFile: File,
    instrumentor: RuntimeInstrumentor
) {
    var hooks by remember { mutableStateOf(instrumentor.getHooks()) }
    var showAddHookDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with actions
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Build,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Runtime Instrumentation",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Modify code behavior at runtime (Frida-like)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddHookDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Hook")
            }

            OutlinedButton(
                onClick = {
                    instrumentor.clearHooks()
                    hooks = instrumentor.getHooks()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All")
            }

            OutlinedButton(
                onClick = {
                    // Export hooks
                    val outputFile = File(apkFile.parentFile, "hooks_script.txt")
                    instrumentor.exportHooksToScript(outputFile)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Instrumentation Presets
        Text("Quick Presets", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = {
                    // Add SSL bypass hooks
                    instrumentor.bypassSSLPinning().forEach { instrumentor.addHook(it) }
                    hooks = instrumentor.getHooks()
                },
                label = { Text("Bypass SSL") },
                leadingIcon = { Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp)) }
            )

            FilterChip(
                selected = false,
                onClick = {
                    // Will trace network when DEX classes are available
                    hooks = instrumentor.getHooks()
                },
                label = { Text("Trace Network") },
                leadingIcon = { Icon(Icons.Default.Cloud, null, modifier = Modifier.size(16.dp)) }
            )

            FilterChip(
                selected = false,
                onClick = {
                    // Will monitor crypto when DEX classes are available
                    hooks = instrumentor.getHooks()
                },
                label = { Text("Monitor Crypto") },
                leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hooks List
        if (hooks.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Build,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hooks configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Add hooks to modify runtime behavior",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hooks) { hook ->
                    HookCard(
                        hook = hook,
                        onDelete = {
                            instrumentor.removeHook(hook.targetClass, hook.targetMethod)
                            hooks = instrumentor.getHooks()
                        }
                    )
                }
            }
        }
    }

    if (showAddHookDialog) {
        AddHookDialog(
            onDismiss = { showAddHookDialog = false },
            onAdd = { className, methodName, hookType, code ->
                instrumentor.addHook(
                    InstrumentationHook(
                        targetClass = className,
                        targetMethod = methodName,
                        hookType = hookType,
                        hookCode = code
                    )
                )
                hooks = instrumentor.getHooks()
                showAddHookDialog = false
            },
            instrumentor = instrumentor
        )
    }
}

@Composable
fun HookCard(
    hook: InstrumentationHook,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (hook.hookType) {
                        HookType.BEFORE -> Icons.Default.ArrowForward
                        HookType.AFTER -> Icons.Default.ArrowBack
                        HookType.REPLACE -> Icons.Default.SwapHoriz
                    },
                    null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${hook.targetClass}.${hook.targetMethod}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Type: ${hook.hookType.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Hook Code:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    hook.hookCode,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AddHookDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, HookType, String) -> Unit,
    instrumentor: RuntimeInstrumentor
) {
    var className by remember { mutableStateOf("") }
    var methodName by remember { mutableStateOf("") }
    var hookType by remember { mutableStateOf(HookType.BEFORE) }
    var hookCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Instrumentation Hook") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Target Class") },
                    placeholder = { Text("com.example.MyClass") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = methodName,
                    onValueChange = { methodName = it },
                    label = { Text("Target Method") },
                    placeholder = { Text("methodName") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Hook Type:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = hookType == HookType.BEFORE,
                        onClick = {
                            hookType = HookType.BEFORE
                            hookCode = instrumentor.generateHookTemplate(className, methodName, HookType.BEFORE)
                        },
                        label = { Text("BEFORE") }
                    )
                    FilterChip(
                        selected = hookType == HookType.AFTER,
                        onClick = {
                            hookType = HookType.AFTER
                            hookCode = instrumentor.generateHookTemplate(className, methodName, HookType.AFTER)
                        },
                        label = { Text("AFTER") }
                    )
                    FilterChip(
                        selected = hookType == HookType.REPLACE,
                        onClick = {
                            hookType = HookType.REPLACE
                            hookCode = instrumentor.generateHookTemplate(className, methodName, HookType.REPLACE)
                        },
                        label = { Text("REPLACE") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        hookCode = instrumentor.generateHookTemplate(className, methodName, hookType)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Template")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hookCode,
                    onValueChange = { hookCode = it },
                    label = { Text("Hook Code") },
                    placeholder = { Text("Log.d(\"Hook\", \"Method called\");") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    maxLines = Int.MAX_VALUE,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (className.isNotBlank() && methodName.isNotBlank() && hookCode.isNotBlank()) {
                        onAdd(className, methodName, hookType, hookCode)
                    }
                },
                enabled = className.isNotBlank() && methodName.isNotBlank() && hookCode.isNotBlank()
            ) {
                Text("Add Hook")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
