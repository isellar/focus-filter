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
import com.focusfilter.R
import com.focusfilter.data.ThemeSetting
import com.focusfilter.ui.theme.FocusFilterTheme
import com.focusfilter.ui.theme.LocalCustomColors
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
            hasPostNotificationPermission = true // Not needed for older versions
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Focus Filter",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!hasNotificationAccess) {
                NotificationAccessCard()
            } else {
                ServiceStatusCard(isPassthroughEnabled = isPassthroughEnabled)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notification History",
                style = MaterialTheme.typography.headlineSmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationHistoryItem(notification)
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(isPassthroughEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Service Status",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val statusText = if (isPassthroughEnabled) "Running (Passthrough)" else "Running (Filtering)"
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PermissionsCheckCard(
    hasCalendar: Boolean,
    hasLocation: Boolean,
    hasPost: Boolean,
    onRequest: () -> Unit
) {
    val allPermissionsGranted = hasCalendar && hasLocation && hasPost
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (allPermissionsGranted) MaterialTheme.colorScheme.primaryContainer else LocalCustomColors.current.warningContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permissions Status", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            PermissionStatus("Read Calendar", hasCalendar)
            PermissionStatus("Access Location", hasLocation)
            PermissionStatus("Post Notifications", hasPost)
            if (!allPermissionsGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant Missing Permissions")
                }
            }
        }
    }
}
@Composable
private fun PermissionStatus(name: String, isGranted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text(
            text = if (isGranted) "GRANTED" else "DENIED",
            fontWeight = FontWeight.Bold,
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}


@Composable
private fun NotificationHistoryItem(notification: NotificationEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notification.title, fontWeight = FontWeight.Bold)
            Text(notification.body)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Category: ${notification.classification}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NotificationAccessCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Notification Access Required",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enable notification access for Focus Filter to work.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            ) {
                Text("Enable Notification Access")
            }
        }
    }
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


/**
 * Checks if notification listener service is enabled.
 */
private fun isNotificationServiceEnabled(context: android.content.Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return flat?.contains(packageName) == true
}
