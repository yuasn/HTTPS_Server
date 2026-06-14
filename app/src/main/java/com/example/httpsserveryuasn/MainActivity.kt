package com.example.httpsserveryuasn

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.httpsserveryuasn.ui.config.ConfigScreen
import com.example.httpsserveryuasn.ui.dashboard.DashboardScreen
import com.example.httpsserveryuasn.ui.theme.HttpsServerYuasnTheme
import com.example.httpsserveryuasn.viewmodel.ServerViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute : NavKey

@Serializable
data object Dashboard : NavRoute

@Serializable
data object Configuration : NavRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HttpsServerYuasnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val serverViewModel: ServerViewModel = viewModel()
    val backStack = rememberNavBackStack(Dashboard)
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.manage_storage_title)) },
            text = { Text(stringResource(R.string.manage_storage_message)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text(stringResource(R.string.grant_permission))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            serverViewModel.startServer()
        }
    }

    val dirPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            serverViewModel.updateWebDirUri(it)
        }
    }

    val certPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            serverViewModel.updateCertFileUri(it)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize()
    ) { key ->
        when (key) {
            is Dashboard -> {
                NavEntry(key) {
                    val isRunning by serverViewModel.isRunning.collectAsState()
                    val logStr by serverViewModel.logs.collectAsState()
                    val logs = logStr.split("\n").filter { it.isNotBlank() }
                    val ipAddresses by serverViewModel.ipAddresses.collectAsState()
                    val port by serverViewModel.port.collectAsState()
                    
                    DashboardScreen(
                        isRunning = isRunning,
                        logs = logs,
                        ipAddresses = ipAddresses,
                        port = port,
                        onStart = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                serverViewModel.startServer()
                            }
                        },
                        onStop = { serverViewModel.stopServer() },
                        onOpenBrowser = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://localhost:$port"))
                            context.startActivity(browserIntent)
                        },
                        onNavigateToConfig = { backStack.add(Configuration) }
                    )
                }
            }
            is Configuration -> {
                NavEntry(key) {
                    val port by serverViewModel.port.collectAsState()
                    val webDirPath by serverViewModel.webDirPath.collectAsState()
                    val useSelfSigned by serverViewModel.useSelfSigned.collectAsState()

                    ConfigScreen(
                        port = port,
                        webDirPath = webDirPath,
                        useSelfSigned = useSelfSigned,
                        onPortChange = { serverViewModel.updatePort(it) },
                        onDirPick = { dirPickerLauncher.launch(null) },
                        onCertPick = { certPickerLauncher.launch(arrayOf("*/*")) },
                        onUseSelfSignedChange = { serverViewModel.updateUseSelfSigned(it) },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
            else -> NavEntry(key) { }
        }
    }
}
