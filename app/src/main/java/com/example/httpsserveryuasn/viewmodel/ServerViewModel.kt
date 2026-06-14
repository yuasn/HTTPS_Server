package com.example.httpsserveryuasn.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.httpsserveryuasn.data.repository.SettingsRepository
import com.example.httpsserveryuasn.server.HttpsServerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

import android.content.Intent
import com.example.httpsserveryuasn.server.NetworkUtils
import com.example.httpsserveryuasn.server.ServerForegroundService

/**
 * ViewModel for the HttpsServer app.
 * Acts as a bridge between the [HttpsServerManager] and the Compose UI.
 * Manages server state and persists settings via [SettingsRepository].
 */
class ServerViewModel(application: Application) : AndroidViewModel(application) {
    private val serverManager = HttpsServerManager.getInstance()
    private val repository = SettingsRepository(application)

    val isRunning = serverManager.isRunning
    val logs = serverManager.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Server initialized.")

    val port = repository.portFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 8443)
    val webDirPath = repository.webDirPathFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val useSelfSigned = repository.useSelfSignedFlow.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val certFilePath = repository.certFilePathFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _ipAddresses = MutableStateFlow<List<String>>(emptyList())
    val ipAddresses = _ipAddresses.asStateFlow()

    init {
        updateIpAddresses()
    }

    fun updateIpAddresses() {
        _ipAddresses.value = NetworkUtils.getLocalIpAddresses()
    }

    fun updatePort(newPort: Int) {
        viewModelScope.launch {
            repository.updatePort(newPort)
        }
    }

    fun updateWebDirUri(uri: Uri) {
        val path = getPathFromTreeUri(uri)
        if (path != null) {
            viewModelScope.launch {
                repository.updateWebDirPath(path)
            }
        }
    }

    fun updateCertFileUri(uri: Uri) {
        val path = getPathFromTreeUri(uri) 
        if (path != null) {
            viewModelScope.launch {
                repository.updateCertFilePath(path)
            }
        }
    }

    fun updateUseSelfSigned(use: Boolean) {
        viewModelScope.launch {
            repository.updateUseSelfSigned(use)
        }
    }

    fun startServer() {
        val context = getApplication<Application>()
        val intent = Intent(context, ServerForegroundService::class.java).apply {
            action = ServerForegroundService.ACTION_START
            putExtra(ServerForegroundService.EXTRA_PORT, port.value)
            putExtra(ServerForegroundService.EXTRA_WEB_DIR, webDirPath.value)
            if (!useSelfSigned.value && certFilePath.value.isNotEmpty()) {
                putExtra(ServerForegroundService.EXTRA_CERT_FILE, certFilePath.value)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        updateIpAddresses()
    }

    fun stopServer() {
        val context = getApplication<Application>()
        val intent = Intent(context, ServerForegroundService::class.java).apply {
            action = ServerForegroundService.ACTION_STOP
        }
        context.stopService(intent)
    }

    private fun getPathFromTreeUri(uri: Uri): String? {
        val path = uri.path ?: return null
        val split = path.split(":")
        if (split.size < 2) return null
        
        val documentId = split[1]
        val volumeId = split[0].split("/").lastOrNull() ?: return null

        return if (volumeId == "primary") {
            "${Environment.getExternalStorageDirectory()}/$documentId"
        } else if (volumeId.matches(Regex("[A-Z0-9]{4}-[A-Z0-9]{4}"))) {
            "/storage/$volumeId/$documentId"
        } else {
            // Fallback for some strange URIs
            if (path.contains("primary")) {
                "${Environment.getExternalStorageDirectory()}/$documentId"
            } else {
                "/storage/$volumeId/$documentId"
            }
        }
    }
}
