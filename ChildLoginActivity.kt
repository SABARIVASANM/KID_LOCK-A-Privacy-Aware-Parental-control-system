package com.example.privacyawareinterface

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.monitor.AppBlockerService
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme
import com.example.privacyawareinterface.utils.SharedPrefsHelper

class ChildLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val role = sharedPref.getString("logged_in_role", null)

        // ✅ Auto-login if already logged in as child
        if (role == "child") {
            startActivity(Intent(this, ChildDashboardActivity::class.java))
            finish()
            return
        }

        setContent {
            PrivacyAwareInterfaceTheme {
                ChildLoginScreen(
                    onLoginSuccess = { username ->
                        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("logged_in_role", "child")
                            putString("child_username", username)
                            apply()
                        }

                        SharedPrefsHelper.setProtectionEnabled(this, true)

                        // ✅ Start background protection service once
                        val serviceIntent = Intent(this, AppBlockerService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }

                        // ✅ Check if Accessibility enabled
                        if (!isAccessibilityEnabled()) {
                            Toast.makeText(
                                this,
                                "Please enable Accessibility for PrivacyAwareInterface",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            startActivity(intent)
                        }

                        // ✅ Move to Child Dashboard
                        val intent = Intent(this, ChildDashboardActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val expected = "$packageName/.monitor.AppBlockerAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val isEnabled =
            !TextUtils.isEmpty(enabledServices) && enabledServices.contains(expected)
        val accessibilityEnabled =
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
        return accessibilityEnabled && isEnabled
    }
}

@Composable
fun ChildLoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Child Login", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        onLoginSuccess(username)
                    } else {
                        Toast.makeText(context, "Please enter both fields", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Login")
            }
        }
    }
}
