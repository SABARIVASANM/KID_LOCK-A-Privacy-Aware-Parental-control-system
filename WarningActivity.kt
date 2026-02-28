package com.example.privacyawareinterface

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme

class WarningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkg = intent.getStringExtra("blocked_pkg") ?: "Blocked app"

        setContent {
            PrivacyAwareInterfaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚫 Access Blocked", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(12.dp))
                        Text("The app \"$pkg\" is blocked by your parents.")
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = {
                            startActivity(Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                            finish()
                        }) {
                            Text("Go to Home 🏠")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }) {
                            Text("Accessibility Settings ⚙️")
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // Custom back behavior
        onBackPressedDispatcher.onBackPressed()  // Call the default back behavior here
    }
}
