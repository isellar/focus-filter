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
    var hasCalendarPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasPostNotificationPermission by remember { mutableStateOf(false) }

    fun updatePermissionsState() {
        hasNotificationAccess = isNotificationServiceEnabled(context)
        hasCalendarPermission = context.hasPermission(Manifest.permission.READ_CALENDAR)
        hasLocationPermission = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPostNotificationPermission = context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasPostNotificationPermission = true // Not needed
        }
    }

    val permissionsToRequest = remember { mutableStateListOf<String>() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { updatePermissionsState() }
    )

    LaunchedEffect(Unit) {
        updatePermissionsState()
        if (!hasCalendarPermission) permissionsToRequest.add(Manifest.permission.READ_CALENDAR)
        if (!hasLocationPermission) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPostNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Focus Filter", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            if (!hasNotificationAccess) {
                NotificationAccessCard()
            } else {
                ServiceStatusCard(isPassthroughEnabled = isPassthroughEnabled)
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                // Manual Classification Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NotificationCategory.values().forEach { category ->
                        val isSelected = notification.userClassification == category.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateCategory(category) },
                            label = { Text(category.name.first().toString()) }
                        )
                    }
                }
                // Actionable Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Actionable", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                    Checkbox(checked = notification.isActionable, onCheckedChange = { onUpdateActionable(it) })
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(isPassthroughEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Service Status", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val statusText = if (isPassthroughEnabled) "Running (Passthrough)" else "Running (Filtering)"
            Text(statusText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
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

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun isNotificationServiceEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat?.contains(context.packageName) == true
}
