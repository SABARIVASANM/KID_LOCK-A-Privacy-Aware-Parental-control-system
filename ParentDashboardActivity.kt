package com.example.privacyawareinterface

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme
import com.example.privacyawareinterface.utils.AppPreferenceManager
import com.example.privacyawareinterface.utils.FirebaseManager
import com.example.privacyawareinterface.utils.SharedPrefsHelper
import com.google.firebase.database.*

class ParentDashboardActivity : ComponentActivity() {

    private val childId = "default_child" // Replace if multiple children later
    private var databaseRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Listen to alerts from child device
        startListeningToAlerts(this)

        setContent {
            PrivacyAwareInterfaceTheme {
                ParentDashboardScreen(
                    context = this,
                    onBack = {
                        val intent = Intent(this, RoleSelectionActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    /** ✅ Real-time listener for child alerts */
    private fun startListeningToAlerts(context: Context) {
        val db = FirebaseDatabase.getInstance().reference
        databaseRef = db.child("child_alerts").child(childId)

        databaseRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                val app = map?.get("app") ?: "Unknown"
                val time = map?.get("time") ?: ""
                val message = "⚠️ Child tried to open $app"

                // Show Snackbar alert
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseRef?.removeEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(context: Context, onBack: () -> Unit) {
    var appList by remember { mutableStateOf(listOf<AppPreferenceManager.AppDetail>()) }
    var blockedApps by remember { mutableStateOf(AppPreferenceManager.getBlockedApps(context).toMutableSet()) }

    // Replace this with dynamic child ID if multi-child system implemented
    val childId = "default_child"

    LaunchedEffect(Unit) {
        appList = AppPreferenceManager.getAllUserAppDetails(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "👨‍👩‍👧 Parent Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ✅ Save Blocked Apps — Push to Firebase + Save Locally
                    IconButton(onClick = {
                        val blockedSet = blockedApps.toSet()

                        // Save locally
                        SharedPrefsHelper.saveBlockedApps(context, blockedSet)

                        // Push to Firebase
                        FirebaseManager.saveBlockedApps(childId, blockedSet.toList())

                        Toast.makeText(context, "✅ Blocked apps updated successfully!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F9))
                .padding(padding)
                .padding(12.dp)
        ) {
            Text(
                text = "Manage Child's Apps & Usage",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(appList) { app ->
                    val isBlocked = blockedApps.contains(app.packageName)
                    val usageMs = SharedPrefsHelper.getAppUsage(context, app.packageName)
                    val usageMins = (usageMs / 60000).toInt()

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
                            app.icon?.let {
                                Image(
                                    bitmap = it.toBitmap().asImageBitmap(),
                                    contentDescription = app.appName,
                                    modifier = Modifier.size(48.dp)
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.LightGray)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Usage: $usageMins min",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Button(
                                    onClick = {
                                        AppPreferenceManager.allowApp(context, app.packageName)
                                        blockedApps = AppPreferenceManager.getBlockedApps(context).toMutableSet()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF81C784),
                                        disabledContainerColor = Color(0xFFB2DFDB)
                                    ),
                                    enabled = isBlocked
                                ) {
                                    Text("Allow")
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        AppPreferenceManager.blockApp(context, app.packageName)
                                        blockedApps = AppPreferenceManager.getBlockedApps(context).toMutableSet()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE57373),
                                        disabledContainerColor = Color(0xFFFFCDD2)
                                    ),
                                    enabled = !isBlocked
                                ) {
                                    Text("Block")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    appList = AppPreferenceManager.getAllUserAppDetails(context)
                    blockedApps = AppPreferenceManager.getBlockedApps(context).toMutableSet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF64B5F6))
            ) {
                Text("🔄 Refresh Apps", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
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
