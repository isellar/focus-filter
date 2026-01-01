package com.focusfilter.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.focusfilter.data.room.NotificationEntity
import com.focusfilter.data.ThemeSetting
import com.focusfilter.models.NotificationCategory
import com.focusfilter.ui.theme.FocusFilterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeSetting by viewModel.themeSetting.collectAsState()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
            }

            FocusFilterTheme(darkTheme = useDarkTheme) {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val isPassthroughEnabled by viewModel.isPassthroughEnabled.collectAsState()
    var hasNotificationAccess by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { /* State will be updated by LaunchedEffect */ }
    )

    LaunchedEffect(Unit) {
        hasNotificationAccess = isNotificationServiceEnabled(context)
        // Request permissions if not granted
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Focus Filter", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 16.dp))
            Spacer(Modifier.height(16.dp))

            if (!hasNotificationAccess) {
                NotificationAccessCard()
            } else {
                ServiceStatusCard(
                    isPassthroughEnabled = isPassthroughEnabled,
                    onToggle = { viewModel.setPassthroughEnabled(it) }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Notification History", style = MaterialTheme.typography.headlineSmall)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationHistoryItem(
                        notification = notification,
                        onUpdateCategory = { category -> viewModel.updateUserClassification(notification.id, category) },
                        onUpdateActionable = { isActionable -> viewModel.updateActionable(notification.id, isActionable) }
                    )
                }
            }

            Button(
                onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(isPassthroughEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Service Status", style = MaterialTheme.typography.titleMedium)
                val statusText = if (isPassthroughEnabled) "Passthrough" else "Filtering"
                Text(statusText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            Switch(checked = !isPassthroughEnabled, onCheckedChange = { onToggle(!it) })
        }
    }
}

@Composable
private fun NotificationHistoryItem(
    notification: NotificationEntity,
    onUpdateCategory: (NotificationCategory) -> Unit,
    onUpdateActionable: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(notification.appName, style = MaterialTheme.typography.labelMedium)
            Text(notification.title, fontWeight = FontWeight.Bold)
            if (notification.body.isNotEmpty()) {
                Text(notification.body, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Manual Classification Icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryIconButton(
                        category = NotificationCategory.URGENT,
                        isSelected = notification.userClassification == NotificationCategory.URGENT.name,
                        onClick = onUpdateCategory
                    )
                    CategoryIconButton(
                        category = NotificationCategory.INFORMATIONAL,
                        isSelected = notification.userClassification == NotificationCategory.INFORMATIONAL.name,
                        onClick = onUpdateCategory
                    )
                    CategoryIconButton(
                        category = NotificationCategory.BACKGROUND,
                        isSelected = notification.userClassification == NotificationCategory.BACKGROUND.name,
                        onClick = onUpdateCategory
                    )
                    CategoryIconButton(
                        category = NotificationCategory.IRRELEVANT,
                        isSelected = notification.userClassification == NotificationCategory.IRRELEVANT.name,
                        onClick = onUpdateCategory
                    )
                }
                // Actionable Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Actionable", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(4.dp))
                    Checkbox(checked = notification.isActionable, onCheckedChange = { onUpdateActionable(it) })
                }
            }
        }
    }
}

@Composable
private fun CategoryIconButton(
    category: NotificationCategory,
    isSelected: Boolean,
    onClick: (NotificationCategory) -> Unit
) {
    val icon = when (category) {
        NotificationCategory.URGENT -> Icons.Default.KeyboardDoubleArrowUp
        NotificationCategory.INFORMATIONAL -> Icons.Default.ArrowUpward
        NotificationCategory.BACKGROUND -> Icons.Default.ArrowDownward
        NotificationCategory.IRRELEVANT -> Icons.Default.Block
    }
    IconToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick(category) }
    ) {
        Icon(imageVector = icon, contentDescription = category.name)
    }
}

@Composable
private fun NotificationAccessCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Notification Access Required", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Please enable notification access for Focus Filter to work.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                Text("Enable Notification Access")
            }
        }
    }
}

private fun isNotificationServiceEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(context.packageName) == true
}
