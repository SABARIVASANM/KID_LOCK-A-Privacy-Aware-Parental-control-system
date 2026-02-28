package com.example.privacyawareinterface

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme

// 🎬 Model for Offline Videos
data class LocalVideo(val resId: Int, val title: String)

class VideoLearningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivacyAwareInterfaceTheme {
                VideoLearningScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLearningScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // ✅ Two Offline Videos
    val videos = listOf(
        LocalVideo(R.raw.privacy_tips_1, "Online Privacy Awareness 🎬"),
        LocalVideo(R.raw.privacy_tips_2, "Being Safe On the Internet 🎬")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "💻 Video Learning",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("🔙") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE3F2FD))
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (videos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No videos available 🎥", color = Color.Gray)
                }
            } else {
                videos.forEach { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                val intent = Intent(context, VideoPlayerActivity::class.java)
                                intent.putExtra("videoResId", video.resId)
                                intent.putExtra("videoTitle", video.title)
                                context.startActivity(intent)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(video.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Text("Tap to play ▶️", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
