package com.example.privacyawareinterface

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme

class RoleSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Auto-direct based on saved role
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        when (sharedPref.getString("logged_in_role", null)) {
            "child" -> {
                startActivity(Intent(this, ChildDashboardActivity::class.java))
                finish()
                return
            }
            "parent" -> {
                startActivity(Intent(this, ParentDashboardActivity::class.java))
                finish()
                return
            }
        }

        setContent {
            PrivacyAwareInterfaceTheme {
                RoleSelectionScreen()
            }
        }
    }
}

@Composable
fun RoleSelectionScreen() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Select Your Role",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(8.dp)
            ) {
                Text("Parent", fontSize = 20.sp)
            }

            Button(
                onClick = {
                    val intent = Intent(context, ChildLoginActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(8.dp)
            ) {
                Text("Child", fontSize = 20.sp)
            }
        }
    }
}
