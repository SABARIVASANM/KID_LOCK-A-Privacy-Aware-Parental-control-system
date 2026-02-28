package com.example.privacyawareinterface

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.monitor.AppBlockerService
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme
import com.example.privacyawareinterface.utils.SharedPrefsHelper
import com.example.privacyawareinterface.utils.FirebaseManager

class ChildDashboardActivity : ComponentActivity() {
    private val usageTimes = mutableMapOf<String, Long>()
    private val lastLaunchTimes = mutableMapOf<String, Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Keep protection running
        if (!SharedPrefsHelper.isProtectionEnabled(this)) {
            SharedPrefsHelper.setProtectionEnabled(this, true)
        }

        val serviceIntent = Intent(this, AppBlockerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }


        // ✅ Realtime Firebase Sync for Blocked Apps
        val childId = "default_child"
        FirebaseManager.listenBlockedApps(childId) { list ->
            val set = list.toSet()
            SharedPrefsHelper.saveBlockedApps(this, set)
        }

        setContent {
            PrivacyAwareInterfaceTheme {
                ChildDashboardScreen(context = this, usageTimes = usageTimes)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val end = SystemClock.elapsedRealtime()
        lastLaunchTimes.forEach { (pkg, start) ->
            val duration = end - start
            usageTimes[pkg] = (usageTimes[pkg] ?: 0L) + duration
            SharedPrefsHelper.saveAppUsage(this, pkg, usageTimes[pkg] ?: 0L)
        }
        lastLaunchTimes.clear()
    }

    fun launchAppFromDashboard(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                lastLaunchTimes[packageName] = SystemClock.elapsedRealtime()
                startActivity(launchIntent)
            }
        } catch (_: Exception) {
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDashboardScreen(context: Context, usageTimes: Map<String, Long>) {
    var allowedApps by remember {
        mutableStateOf(
            try {
                SharedPrefsHelper.getAllowedApps(context)
            } catch (e: Exception) {
                emptySet<String>()
            }
        )
    }

    val packageManager = context.packageManager
    val appList = remember { mutableStateListOf<ApplicationInfo>() }

    LaunchedEffect(allowedApps) {
        appList.clear()
        allowedApps.forEach { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                appList.add(appInfo)
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "👦 Child Dashboard",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit().clear().apply()
                        val intent = Intent(context, RoleSelectionActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (context is ComponentActivity) context.finish()
                    }) {
                        Text("🔙", fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF8DC))
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(16.dp)
        ) {
            if (appList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps allowed yet.\nPlease ask your parent to approve some apps.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(appList) { appInfo ->
                        val icon = appInfo.loadIcon(packageManager)
                        val appName = appInfo.loadLabel(packageManager).toString()
                        val packageName = appInfo.packageName
                        val usageTime = SharedPrefsHelper.getAppUsage(context, packageName)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = icon.toBitmap().asImageBitmap(),
                                    contentDescription = appName,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Usage: ${formatDuration(usageTime)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (context is ChildDashboardActivity) {
                                            context.launchAppFromDashboard(packageName)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF64B5F6)
                                    )
                                ) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    allowedApps = try {
                        SharedPrefsHelper.getAllowedApps(context)
                    } catch (e: Exception) {
                        emptySet()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
            ) {
                Text("🔄 Refresh", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d min %02d sec", minutes, seconds)
}

private fun Drawable.toBitmap(): Bitmap {
    return if (this is BitmapDrawable && this.bitmap != null) {
        this.bitmap
    } else {
        val width = if (intrinsicWidth > 0) intrinsicWidth else 48
        val height = if (intrinsicHeight > 0) intrinsicHeight else 48
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap
    }
}
