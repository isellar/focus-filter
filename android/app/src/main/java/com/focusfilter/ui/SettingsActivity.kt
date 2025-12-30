package com.focusfilter.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusfilter.R
import com.focusfilter.data.ThemeSetting
import com.focusfilter.ui.theme.FocusFilterTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeSetting by viewModel.themeSetting.collectAsState()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
            }

            FocusFilterTheme(darkTheme = useDarkTheme) {
                SettingsScreen(viewModel)
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val exportResult by viewModel.exportResult.collectAsState()
    val isPassthroughEnabled by viewModel.passthroughEnabled.collectAsState()
    val apiEndpoint by viewModel.apiEndpoint.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()

    // When the exportResult has a value, launch the share sheet.
    LaunchedEffect(exportResult) {
        exportResult?.let { json ->
            val file = saveJsonToFile(context, json)
            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                shareJson(context, uri)
            }
            viewModel.onExportConsumed()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Divider()

            // Theme settings
            ThemeSettingsSection(
                currentSetting = themeSetting,
                onSettingChanged = { viewModel.setThemeSetting(it) }
            )
            Divider()

            // Passthrough setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Passthrough Mode", style = MaterialTheme.typography.bodyLarge)
                    Text("Don't suppress notifications; just record them.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isPassthroughEnabled, onCheckedChange = { viewModel.setPassthroughEnabled(it) })
            }
            Divider()

            // API Endpoint setting
            Column {
                Text("API Endpoint", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiEndpoint,
                    onValueChange = { viewModel.setApiEndpoint(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Endpoint URL") }
                )
                Spacer(Modifier.height(8.dp))
                Text("For development, use http://10.0.2.2:8000/", style = MaterialTheme.typography.bodySmall)
            }
            Divider()

            // Export setting
            Column {
                Button(onClick = { viewModel.exportNotifications() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Export Notifications to JSON")
                }
                Spacer(Modifier.height(8.dp))
                Text("Export all processed notifications to a shareable JSON file.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ThemeSettingsSection(currentSetting: ThemeSetting, onSettingChanged: (ThemeSetting) -> Unit) {
    val options = ThemeSetting.values()
    Column {
        Text("Theme", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.selectableGroup()) {
            options.forEach { option ->
                Row(
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = (option == currentSetting),
                            onClick = { onSettingChanged(option) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (option == currentSetting), onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(option.name)
                }
            }
        }
    }
}

private fun saveJsonToFile(context: Context, json: String): File? {
    return try {
        val file = File(context.cacheDir, "notifications.json")
        FileOutputStream(file).use { it.write(json.toByteArray()) }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun shareJson(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Notifications JSON"))
}
