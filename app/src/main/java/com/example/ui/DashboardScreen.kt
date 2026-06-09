package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameState
import com.example.viewmodel.PregnancyViewModel
import com.example.data.GameProfile
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: PregnancyViewModel,
    profile: GameProfile,
    onNavigateToTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Game stats & collections
    val gameState by viewModel.gameState.collectAsState()
    val grid by viewModel.grid.collectAsState()
    val playerPos by viewModel.playerPosition.collectAsState()
    val enemies by viewModel.enemies.collectAsState()
    val score by viewModel.score.collectAsState()
    val level by viewModel.level.collectAsState()
    val survivalSecs by viewModel.survivalSeconds.collectAsState()

    // Stylized theme colors
    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF39FF14)
    val darkBg = Color(0xFF0A0915)
    val gridLineColor = Color(0xFF1E1C38)

    // Gesture swipe detection variables
    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }
    var dragTriggered by remember { mutableStateOf(false) }

    // Pulsing animations for Game Over text and items
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchPulsing")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. GAME PLAY HEADER STATS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
                    .background(Color(0xFF131124), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Score metric
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "SCORE",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "%04d".format(score),
                        fontSize = 20.sp,
                        color = neonGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Level indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "L-$level",
                        fontSize = 20.sp,
                        color = neonBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Survival Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TIME",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "%02d:%02d".format(survivalSecs / 60, survivalSecs % 60),
                        fontSize = 20.sp,
                        color = neonPink,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2. STAGE/GAME-STATE BOARD AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = neonBlue, spotColor = neonPink)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, neonBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F0E20))
                    // Swipe gesture hook
                    .pointerInput(gameState) {
                        if (gameState != GameState.PLAYING) return@pointerInput
                        detectDragGestures(
                            onDragStart = {
                                totalDragX = 0f
                                totalDragY = 0f
                                dragTriggered = false
                            },
                            onDragEnd = {
                                dragTriggered = false
                            },
                            onDragCancel = {
                                dragTriggered = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (!dragTriggered) {
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y

                                    val threshold = 55f // sensitivity distance pixel threshold
                                    if (Math.abs(totalDragX) > threshold || Math.abs(totalDragY) > threshold) {
                                        dragTriggered = true
                                        if (Math.abs(totalDragX) > Math.abs(totalDragY)) {
                                            if (totalDragX > 0) {
                                                viewModel.movePlayer(0, 1) // Right
                                            } else {
                                                viewModel.movePlayer(0, -1) // Left
                                            }
                                        } else {
                                            if (totalDragY > 0) {
                                                viewModel.movePlayer(1, 0) // Down
                                            } else {
                                                viewModel.movePlayer(-1, 0) // Up
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // If grid is initialized, render the arcade coordinate items
                if (grid.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (r in 0 until viewModel.gridSize) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (c in 0 until viewModel.gridSize) {
                                    val cellType = grid[r][c]
                                    val currentPos = Pair(r, c)
                                    val isPlayer = playerPos == currentPos

                                    // Find any enemy occupying current cell
                                    val enemyOnCell = enemies.find { it.position == currentPos }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    cellType == 1 -> Color(0xFF242240) // Wall blocker obsidian
                                                    isPlayer -> neonBlue.copy(alpha = 0.15f) // Subtle trail
                                                    enemyOnCell != null -> Color(0xFFFF003C).copy(alpha = 0.12f)
                                                    else -> Color(0xFF0F0E20) // Clean path
                                                }
                                            )
                                            .border(
                                                width = if (cellType == 1) 0.5.dp else 0.dp,
                                                color = if (cellType == 1) Color(0xFF383561) else Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Draw graphics on cells (runner, chaser, diamond gems, or blocks)
                                        when {
                                            isPlayer -> {
                                                // Player: Glowing neon cyan runner
                                                Text(
                                                    text = "🏃",
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.shadow(4.dp, CircleShape)
                                                )
                                            }
                                            enemyOnCell != null -> {
                                                // Enemy: Red skull or custom drone
                                                val emoji = when (enemyOnCell.type) {
                                                    com.example.viewmodel.EnemyType.STALKER -> "🤖" // Red Hunter Bot
                                                    com.example.viewmodel.EnemyType.AMBUSH -> "👾" // Purple Alien Interceptor
                                                    com.example.viewmodel.EnemyType.WANDERER -> "🛸" // Fast Drone
                                                }
                                                Text(
                                                    text = emoji,
                                                    fontSize = 18.sp
                                                )
                                            }
                                            cellType == 4 -> {
                                                // Gem: Glowing high-voltage battery cell
                                                Text(
                                                    text = "⚡",
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.shadow(2.dp, CircleShape, ambientColor = neonGreen, spotColor = neonGreen)
                                                )
                                            }
                                            cellType == 1 -> {
                                                // Wall: Subtle pixel pattern
                                                Spacer(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(Color(0xFF4C4680), RoundedCornerShape(1.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2B. GAME CONTROL SCREEN OVERLAYS
                androidx.compose.animation.AnimatedVisibility(
                    visible = gameState == GameState.MENU,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xE60A0915)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "FLIGHT CONSOLE ACTIVE",
                                color = neonBlue,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                letterSpacing = 3.sp
                            )
                            Text(
                                text = "Welcome, ${profile.username}!",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Complete the grid by collecting all glowing high-voltage power cells (⚡) while evading the Hunter bots (🤖, 👾, 🛸).\n\nUse swipe gestures on the grid, or the cyber D-pad controller below to steer.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Button(
                                onClick = { viewModel.startNewGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = neonPink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = neonPink, spotColor = neonPink)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                    Text(
                                        "START EVASION RUN",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = gameState == GameState.PAUSED,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xD90A0915)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "SYSTEM STANDBY",
                                color = neonBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "Gamer profile: ${profile.username}\nGame state temporarily suspended.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.startNewGame() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = ButtonDefaults.outlinedButtonBorder.copy()
                                ) {
                                    Text("RESTART", fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    onClick = { viewModel.resumeGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
                                ) {
                                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = gameState == GameState.GAME_OVER,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xF20A0915)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "HULL BREACHED !",
                                color = neonPink,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.shadow(4.dp, CircleShape),
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "RUN TERMINATED",
                                color = Color.White.copy(alpha = alphaPulse),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141226)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("PILOT:", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        Text(profile.username, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("FINAL SCORE:", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        Text("%04d".format(score), color = neonGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("SURVIVED:", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        Text("%02d:%02d".format(survivalSecs / 60, survivalSecs % 60), color = neonPink, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("LEVEL ARRIVED:", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        Text("L-$level", color = neonBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.startNewGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = neonBlue, spotColor = neonBlue)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black)
                                    Text(
                                        "REDEPLOY SIMULATOR",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. RETRO CYBER METRO D-PAD CONTROLLER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // D-Pad Up
                    IconButton(
                        onClick = { viewModel.movePlayer(-1, 0) },
                        enabled = gameState == GameState.PLAYING,
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(2.dp, CircleShape)
                            .background(
                                color = if (gameState == GameState.PLAYING) Color(0xFF1B163B) else Color(0xFF131124),
                                shape = CircleShape
                            )
                            .border(1.5.dp, if (gameState == GameState.PLAYING) neonBlue else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (gameState == GameState.PLAYING) neonBlue else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // D-Pad Left
                        IconButton(
                            onClick = { viewModel.movePlayer(0, -1) },
                            enabled = gameState == GameState.PLAYING,
                            modifier = Modifier
                                .size(54.dp)
                                .shadow(2.dp, CircleShape)
                                .background(
                                    color = if (gameState == GameState.PLAYING) Color(0xFF1B163B) else Color(0xFF131124),
                                    shape = CircleShape
                                )
                                .border(1.5.dp, if (gameState == GameState.PLAYING) neonBlue else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Move Left",
                                tint = if (gameState == GameState.PLAYING) neonBlue else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Center Pause/State Action Element
                        IconButton(
                            onClick = {
                                when (gameState) {
                                    GameState.PLAYING -> viewModel.pauseGame()
                                    GameState.PAUSED -> viewModel.resumeGame()
                                    GameState.GAME_OVER -> viewModel.startNewGame()
                                    else -> {}
                                }
                            },
                            enabled = gameState != GameState.ONBOARDING,
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(6.dp, CircleShape)
                                .background(
                                    color = when (gameState) {
                                        GameState.PLAYING -> neonGreen.copy(alpha = 0.15f)
                                        GameState.PAUSED -> neonPink.copy(alpha = 0.15f)
                                        else -> Color(0xFF1B163B)
                                    },
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = when (gameState) {
                                        GameState.PLAYING -> neonGreen
                                        GameState.PAUSED -> neonPink
                                        else -> Color.White.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = when (gameState) {
                                    GameState.PLAYING -> Icons.Default.Pause
                                    GameState.PAUSED -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Gamepad
                                },
                                contentDescription = "Game controls",
                                tint = when (gameState) {
                                    GameState.PLAYING -> neonGreen
                                    GameState.PAUSED -> neonPink
                                    else -> Color.White
                                },
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // D-Pad Right
                        IconButton(
                            onClick = { viewModel.movePlayer(0, 1) },
                            enabled = gameState == GameState.PLAYING,
                            modifier = Modifier
                                .size(54.dp)
                                .shadow(2.dp, CircleShape)
                                .background(
                                    color = if (gameState == GameState.PLAYING) Color(0xFF1B163B) else Color(0xFF131124),
                                    shape = CircleShape
                                )
                                .border(1.5.dp, if (gameState == GameState.PLAYING) neonBlue else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Move Right",
                                tint = if (gameState == GameState.PLAYING) neonBlue else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // D-Pad Down
                    IconButton(
                        onClick = { viewModel.movePlayer(1, 0) },
                        enabled = gameState == GameState.PLAYING,
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(2.dp, CircleShape)
                            .background(
                                color = if (gameState == GameState.PLAYING) Color(0xFF1B163B) else Color(0xFF131124),
                                shape = CircleShape
                            )
                            .border(1.5.dp, if (gameState == GameState.PLAYING) neonBlue else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (gameState == GameState.PLAYING) neonBlue else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
