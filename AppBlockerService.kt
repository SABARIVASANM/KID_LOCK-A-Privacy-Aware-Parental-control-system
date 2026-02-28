package com.example.privacyawareinterface.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.privacyawareinterface.WarningActivity
import com.example.privacyawareinterface.utils.FirebaseManager
import com.example.privacyawareinterface.utils.SharedPrefsHelper
import kotlinx.coroutines.*

/**
 * Foreground service that receives the current foreground package (from AccessibilityService)
 * and checks blocked list. If blocked -> show WarningActivity and send alert to Firebase.
 *
 * NOTE: this service should be started as a foreground service (startForeground).
 */
class AppBlockerService : Service() {
    companion object {
        private const val TAG = "AppBlockerService"
        private const val CHANNEL_ID = "privacy_protection_channel_v1"
        private const val NOTIF_ID = 1001
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cachedBlocked: Set<String> = emptySet()
    private var childId: String = "child_default" // set this to real child id if you have multi-child support

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Parental protection active"))
        // load from shared prefs
        cachedBlocked = SharedPrefsHelper.getBlockedApps(this)
        Log.d(TAG, "service created, blocked=${cachedBlocked.size}")
        // optionally: start a coroutine to refresh blocked list periodically
        scope.launch {
            while (isActive) {
                delay(5_000) // refresh every 5s
                cachedBlocked = SharedPrefsHelper.getBlockedApps(this@AppBlockerService)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == AppBlockerAccessibilityService.ACTION_FOREGROUND_PKG) {
                val pkg = it.getStringExtra(AppBlockerAccessibilityService.EXTRA_FOREGROUND_PKG)
                pkg?.let { p -> checkPackage(p) }
            }
        }
        // keep service running
        return START_STICKY
    }

    private fun checkPackage(pkg: String) {
        try {
            // refresh cache on demand
            cachedBlocked = SharedPrefsHelper.getBlockedApps(this)
            if (cachedBlocked.contains(pkg)) {
                Log.d(TAG, "Blocked app opened: $pkg")
                // send an alert to parent via Firebase
                FirebaseManager.sendAlert(childId, pkg)

                // launch warning screen (brings to front)
                val warn = Intent(this, WarningActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("blocked_pkg", pkg)
                }
                startActivity(warn)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPackage error", e)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = Intent(this, WarningActivity::class.java).apply {
            putExtra("blocked_pkg", "Parental protection")
        }
        val pending = PendingIntent.getActivity(
            this, 0, openIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parental Protection")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Parental protection", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
