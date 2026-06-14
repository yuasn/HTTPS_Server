package com.example.httpsserveryuasn.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.httpsserveryuasn.MainActivity
import com.example.httpsserveryuasn.R
import java.io.File

class ServerForegroundService : Service() {

    private val serverManager = HttpsServerManager.getInstance()

    companion object {
        private const val CHANNEL_ID = "HttpsServerChannel"
        private const val NOTIFICATION_ID = 1
        
        const val ACTION_START = "START_SERVER"
        const val ACTION_STOP = "STOP_SERVER"
        
        const val EXTRA_PORT = "PORT"
        const val EXTRA_WEB_DIR = "WEB_DIR"
        const val EXTRA_CERT_FILE = "CERT_FILE"
        const val EXTRA_KEY_PWD = "KEY_PWD"
        const val EXTRA_KEYSTORE_PWD = "KEYSTORE_PWD"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val port = intent.getIntExtra(EXTRA_PORT, 8443)
                val webDirPath = intent.getStringExtra(EXTRA_WEB_DIR) ?: ""
                val certFilePath = intent.getStringExtra(EXTRA_CERT_FILE)
                val keyPwd = intent.getStringExtra(EXTRA_KEY_PWD) ?: "password"
                val keystorePwd = intent.getStringExtra(EXTRA_KEYSTORE_PWD) ?: "password"

                val webDir = File(webDirPath)
                val certFile = certFilePath?.let { File(it) }

                serverManager.start(port, webDir, certFile, keyPwd, keystorePwd)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        createNotification(port),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, createNotification(port))
                }
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serverManager.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(port: Int): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content, port))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
