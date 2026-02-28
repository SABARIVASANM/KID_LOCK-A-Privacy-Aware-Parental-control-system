package com.example.privacyawareinterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme

data class TheoryPage(
    val title: String,
    val description: String,
    val emoji: String,
    val imageRes: Int
)

class TheoryLearningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivacyAwareInterfaceTheme {
                TheoryLearningScreen(onExit = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryLearningScreen(onExit: () -> Unit) {
    val pages = listOf(
        TheoryPage(
            title = "🔒 Keep Your Password Secret",
            description = "Your password is like your superhero mask — only you should know it! Never share it, even with your best friend.",
            emoji = "🦸‍♂️",
            imageRes = R.drawable.privacy_lock
        ),
        TheoryPage(
            title = "📱 Don’t Share Personal Info",
            description = "Never tell anyone online your name, address, school, or phone number. Keep your details private to stay safe!",
            emoji = "🚫",
            imageRes = R.drawable.personal_info
        ),
        TheoryPage(
            title = "👨‍👩‍👧 Ask Parents for Help",
            description = "If you see something strange or scary online, pause and talk to your parents. They’re your best helpers! 💬",
            emoji = "🧠",
            imageRes = R.drawable.parents_help
        ),
        TheoryPage(
            title = "📸 Think Before You Post",
            description = "Once you share something online, it stays there forever! Post only what makes you proud and happy. 📷",
            emoji = "💡",
            imageRes = R.drawable.think_post
        ),
        TheoryPage(
            title = "🛡️ You’re a Privacy Hero!",
            description = "Amazing! You now know how to be safe and smart online. Keep protecting your digital world! 🌟",
            emoji = "🏆",
            imageRes = R.drawable.privacy_hero
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val colors = listOf(
        listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA)),
        listOf(Color(0xFFC8E6C9), Color(0xFFA5D6A7)),
        listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D)),
        listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80)),
        listOf(Color(0xFFD1C4E9), Color(0xFFB39DDB))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Theory Learning", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onExit) { Text("🔙") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF8E1))
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            val page = pages[pageIndex]
            val bgColors = colors[pageIndex % colors.size]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(bgColors))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = page.emoji,
                            fontSize = 40.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = page.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = page.title,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.description,
                            fontSize = 16.sp,
                            color = Color(0xFF424242),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AnimatedVisibility(visible = pageIndex == pages.lastIndex) {
                            Text(
                                text = "🌈 Great Job! You finished all lessons! 👏",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
