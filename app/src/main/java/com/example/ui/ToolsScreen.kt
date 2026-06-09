package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HighScoreEntry
import com.example.viewmodel.PregnancyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ToolsScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val scores by viewModel.highScores.collectAsState()

    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF39FF14)
    val darkBg = Color(0xFF0A0915)
    val cardBg = Color(0xFF141226)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Cyberspace Heading Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF110E24),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "Leaderboard",
                        tint = neonBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "LEADERBOARD",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Top pilots and high-score evasion simulation logs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Main Board Logs Stream
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item 1: Summary highscore ranking or empty placeholder
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ARCADE HALL OF FAME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = neonPink,
                        fontFamily = FontFamily.Monospace
                    )

                    if (scores.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearAllHighScores() },
                            colors = ButtonDefaults.textButtonColors(contentColor = neonPink)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear logs",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WIPE DATA", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            if (scores.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy placeholder",
                                tint = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "NO FLIGHT DATA AVAILABLE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Complete evasion runs from the home screen tab to register and rank your survival records here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(scores) { index, entry ->
                    HighScoreRow(
                        rank = index + 1,
                        item = entry,
                        neonBlue = neonBlue,
                        neonPink = neonPink,
                        neonGreen = neonGreen,
                        cardBg = cardBg
                    )
                }
            }
        }
    }
}

@Composable
fun HighScoreRow(
    rank: Int,
    item: HighScoreEntry,
    neonBlue: Color,
    neonPink: Color,
    neonGreen: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    val dateSdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val timestampFormatted = dateSdf.format(Date(item.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (rank <= 3) 1.dp else 0.dp,
                color = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rank Badge Column
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700).copy(alpha = 0.15f)
                            2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
                            3 -> Color(0xFFCD7F32).copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#$rank"
                    },
                    fontSize = if (rank <= 3) 18.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Pilot and Date Meta Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.username,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = timestampFormatted,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Score details column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%04d PTS".format(item.score),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = neonGreen,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${item.survivalTimeSeconds}s Survival",
                    fontSize = 11.sp,
                    color = neonPink,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
