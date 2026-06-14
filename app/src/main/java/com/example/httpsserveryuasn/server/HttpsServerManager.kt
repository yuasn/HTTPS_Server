package com.example.httpsserveryuasn.server

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.ktor.server.html.*
import io.ktor.server.request.*
import kotlinx.html.*
import java.io.File
import java.security.KeyStore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Manages the lifecycle and configuration of the Ktor HTTPS server.
 * Handles SSL/TLS setup, self-signed certificate generation, and static file serving.
 */
class HttpsServerManager private constructor() {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    
    private val _logs = MutableSharedFlow<String>(replay = 100)
    val logs = _logs.asSharedFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val logFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    companion object {
        @Volatile
        private var instance: HttpsServerManager? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: HttpsServerManager().also { instance = it }
        }
    }

    fun start(
        port: Int,
        webDir: File,
        certFile: File? = null,
        keyPassword: String = "password",
        keystorePassword: String = "password"
    ) {
        if (_isRunning.value) {
            log("Server is already running.")
            return
        }

        scope.launch {
            try {
                log("Starting server on port $port...")
                log("Web Directory: ${webDir.absolutePath}")
                
                if (!webDir.exists()) {
                    log("Warning: Web directory does not exist! Creating it...")
                    webDir.mkdirs()
                }

                val keyStore = if (certFile != null && certFile.exists()) {
                    log("Loading provided keystore: ${certFile.absolutePath}")
                    KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                        certFile.inputStream().use { load(it, keystorePassword.toCharArray()) }
                    }
                } else {
                    log("Generating self-signed certificate...")
                    generateSelfSignedKeyStore(keystorePassword, keyPassword)
                }

                val config = serverConfig {
                    module {
                        install(StatusPages) {
                            exception<Throwable> { call, cause ->
                                log("Error during request: ${cause.localizedMessage}")
                                call.respondText("Internal Server Error: ${cause.message}", status = HttpStatusCode.InternalServerError)
                            }
                        }

                        routing {
                            staticFiles(
                                remotePath = "/",
                                dir = webDir
                            ) {
                                default("index.html")
                                enableAutoHeadResponse()
                            }

                            staticFiles(
                                remotePath = "/files",
                                dir = webDir
                            ) {
                                default("index.html")
                                enableAutoHeadResponse()
                            }
                            
                            // Custom directory listing route
                            route("/browse") {
                                directoryListing(webDir)
                            }
                        }
                    }
                }

                val newServer = embeddedServer(Netty, rootConfig = config, configure = {
                    sslConnector(
                        keyStore = keyStore,
                        keyAlias = "serverAlias",
                        keyStorePassword = { keystorePassword.toCharArray() },
                        privateKeyPassword = { keyPassword.toCharArray() }
                    ) {
                        this.port = port
                    }
                })

                newServer.start(wait = false)
                server = newServer
                _isRunning.value = true
                log("Server started successfully on port $port.")
            } catch (e: Exception) {
                log("Failed to start server: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        if (!_isRunning.value) {
            log("Server is not running.")
            return
        }

        scope.launch {
            try {
                log("Stopping server...")
                server?.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
                server = null
                _isRunning.value = false
                log("Server stopped.")
            } catch (e: Exception) {
                log("Error stopping server: ${e.localizedMessage}")
            }
        }
    }

    private fun generateSelfSignedKeyStore(keystorePassword: String, keyPassword: String): KeyStore {
        val jksKeyStore = buildKeyStore {
            certificate("serverAlias") {
                password = keyPassword
                domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
            }
        }

        // Convert JKS to PKCS12 for better Android compatibility
        val pkcs12KeyStore = KeyStore.getInstance("PKCS12")
        pkcs12KeyStore.load(null, null)

        val alias = "serverAlias"
        val key = jksKeyStore.getKey(alias, keyPassword.toCharArray())
        val chain = jksKeyStore.getCertificateChain(alias)

        pkcs12KeyStore.setKeyEntry(alias, key, keyPassword.toCharArray(), chain)

        return pkcs12KeyStore
    }

    private fun log(message: String) {
        val timestamp = LocalDateTime.now().format(logFormatter)
        scope.launch {
            _logs.emit("[$timestamp] $message")
        }
    }

    private fun Route.directoryListing(baseDir: File) {
        get("{path...}") {
            val relativePath = call.parameters.getAll("path")?.joinToString(File.separator) ?: ""
            val file = File(baseDir, relativePath).normalize()

            if (!file.exists()) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            if (file.isDirectory) {
                call.respondHtml {
                    body {
                        h1 { +"Index of /${relativePath}" }
                        hr()
                        ul {
                            if (relativePath.isNotEmpty()) {
                                li { a(href = "..") { +".. [Parent Directory]" } }
                            }

                            file.listFiles()?.sortedBy { it.name }?.forEach { f ->
                                li {
                                    val suffix = if (f.isDirectory) "/" else ""
                                    val currentPath = call.request.path().removeSuffix("/")
                                    a(href = "$currentPath/${f.name}$suffix") {
                                        +("${f.name}$suffix")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                call.respondFile(file)
            }
        }
    }
}
