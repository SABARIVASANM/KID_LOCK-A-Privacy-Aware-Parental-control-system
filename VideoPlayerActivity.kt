package com.example.privacyawareinterface

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoResId = intent.getIntExtra("videoResId", -1)
        val videoTitle = intent.getStringExtra("videoTitle") ?: "Video Lesson"

        setContent {
            PrivacyAwareInterfaceTheme {
                VideoPlayerScreen(videoResId, videoTitle) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(videoResId: Int, title: String, onExit: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎬 $title") },
                navigationIcon = {
                    IconButton(onClick = onExit) { Text("🔙") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (videoResId != -1) {
                AndroidView(factory = {
                    VideoView(context).apply {
                        val uri = Uri.parse("android.resource://${context.packageName}/$videoResId")
                        setVideoURI(uri)
                        val controller = MediaController(context)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        start()
                    }
                }, modifier = Modifier.fillMaxSize())
            } else {
                Text("⚠️ Video not found", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
