package com.example.privacyawareinterface

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.elevatedButtonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacyawareinterface.ui.theme.PrivacyAwareInterfaceTheme
import kotlinx.coroutines.delay

/**
 * 🧩 Privacy Awareness Quiz for Kids
 * Fun, animated yes/no quiz with scoring and feedback.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class GameLearningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivacyAwareInterfaceTheme {
                GameLearningScreen(
                    onExit = {
                        val intent = Intent(this, ChildDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

data class QuizQuestion(val prompt: String, val correctAnswerIsYes: Boolean)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GameLearningScreen(onExit: () -> Unit) {
    val context = LocalContext.current

    val questions = remember {
        listOf(
            QuizQuestion("Should you share your password with friends?", false),
            QuizQuestion("Is it okay to post your home address publicly?", false),
            QuizQuestion("Should you ask a parent before downloading new apps?", true),
            QuizQuestion("Is sharing your school name on a public post safe?", false),
            QuizQuestion("Should you check with parents before adding new friends online?", true)
        )
    }

    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var answered by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("privacy_game_prefs", Context.MODE_PRIVATE)
    var highScore by remember { mutableIntStateOf(prefs.getInt("privacy_game_high_score", 0)) }

    // Background subtle animation
    val infinite = rememberInfiniteTransition()
    val offset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Celebrate animation
    val celebrateScale = remember { Animatable(1f) }

    LaunchedEffect(highScore, score) {
        if (score > highScore) {
            celebrateScale.snapTo(1f)
            celebrateScale.animateTo(1.15f, tween(350, easing = FastOutSlowInEasing))
            celebrateScale.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
            prefs.edit().putInt("privacy_game_high_score", score).apply()
            highScore = score
        }
    }

    // Move to next question after short delay
    LaunchedEffect(answered) {
        if (answered) {
            delay(700)
            if (index < questions.lastIndex) index++ else showResult = true
            answered = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🧩 Privacy Quiz",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Text("🔙", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB3E5FC).copy(alpha = 0.9f),
                            Color(0xFFD1C4E9).copy(alpha = 0.9f)
                        ),
                        startY = 0f + offset * 300f,
                        endY = 800f - offset * 300f
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 🔹 Progress + Score
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Question ${index + 1} / ${questions.size}", fontSize = 14.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Score", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "$score",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.scale(celebrateScale.value)
                        )
                    }
                }

                // 🔹 Animated Question Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = index,
                        transitionSpec = {
                            (slideInHorizontally { width -> width } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut(tween(180)))
                        }
                    ) { targetIndex ->
                        val q = questions[targetIndex]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = q.prompt,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0D47A1)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AnimatedVisibility(visible = answered) {
                                    val feedback =
                                        if (lastAnswerCorrect) "Correct ✅" else "Not quite ❌"
                                    Text(
                                        text = feedback,
                                        color = if (lastAnswerCorrect) Color(0xFF2E7D32)
                                        else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }

                // 🔹 Yes / No Buttons
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedButton(
                            onClick = {
                                if (!answered) {
                                    val correct = questions[index].correctAnswerIsYes
                                    lastAnswerCorrect = correct
                                    if (correct) score++
                                    answered = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = elevatedButtonColors(containerColor = Color(0xFF81C784))
                        ) {
                            Text("✅ Yes", fontSize = 18.sp, color = Color.White)
                        }

                        ElevatedButton(
                            onClick = {
                                if (!answered) {
                                    val correct = !questions[index].correctAnswerIsYes
                                    lastAnswerCorrect = correct
                                    if (correct) score++
                                    answered = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = elevatedButtonColors(containerColor = Color(0xFFFF8A65))
                        ) {
                            Text("❌ No", fontSize = 18.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            index = 0
                            score = 0
                            answered = false
                            showResult = false
                        }) {
                            Text("Restart")
                        }

                        TextButton(onClick = { showResult = true }) {
                            Text("Finish")
                        }
                    }
                }
            }

            // 🔹 Result Overlay
            if (showResult) {
                if (score > prefs.getInt("privacy_game_high_score", 0)) {
                    prefs.edit().putInt("privacy_game_high_score", score).apply()
                }
                val finalHigh = prefs.getInt("privacy_game_high_score", 0)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val title =
                                if (score >= questions.size * 0.8)
                                    "You’re a Privacy Hero! 🛡️"
                                else "Great try! Keep learning 💡"

                            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Score: $score / ${questions.size}", fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("High Score: $finalHigh", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = {
                                    index = 0
                                    score = 0
                                    showResult = false
                                }) {
                                    Text("Play Again")
                                }
                                OutlinedButton(onClick = onExit) {
                                    Text("Exit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
