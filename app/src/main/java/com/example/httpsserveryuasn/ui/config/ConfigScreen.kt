package com.example.httpsserveryuasn.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.httpsserveryuasn.R
import com.example.httpsserveryuasn.ui.theme.HttpsServerYuasnTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    port: Int,
    webDirPath: String,
    useSelfSigned: Boolean,
    onPortChange: (Int) -> Unit,
    onDirPick: () -> Unit,
    onCertPick: () -> Unit,
    onUseSelfSignedChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.server_configuration)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Port Number
            Text(
                text = stringResource(R.string.network_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = port.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { onPortChange(it) }
                },
                label = { Text(stringResource(R.string.server_port)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Web Directory
            Text(
                text = stringResource(R.string.host_directory),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = webDirPath,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.selected_folder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = onDirPick) {
                        Icon(Icons.Default.Folder, contentDescription = stringResource(R.string.pick_directory))
                    }
                }
            )
            Text(
                text = stringResource(R.string.directory_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SSL/TLS Settings
            Text(
                text = stringResource(R.string.security_ssl_tls),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useSelfSigned,
                            onCheckedChange = onUseSelfSignedChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.use_self_signed))
                    }
                    if (!useSelfSigned) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onCertPick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.select_cert_file))
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.ssl_warning),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigPreview() {
    HttpsServerYuasnTheme {
        ConfigScreen(
            port = 8443,
            webDirPath = "/sdcard/Download",
            useSelfSigned = true,
            onPortChange = {},
            onDirPick = {},
            onCertPick = {},
            onUseSelfSignedChange = {},
            onBack = {}
        )
    }
}
