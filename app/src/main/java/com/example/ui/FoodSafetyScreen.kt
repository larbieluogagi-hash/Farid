package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
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
import com.example.viewmodel.PregnancyViewModel

data class BestiaryItem(
    val entityEmoji: String,
    val name: String,
    val dangerLevel: String, // "LOW", "MODERATE", "CRITICAL"
    val behaviorName: String,
    val description: String,
    val colorHex: String
)

@Composable
fun FoodSafetyScreen(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF39FF14)
    val darkBg = Color(0xFF0A0915)
    val cardBg = Color(0xFF141226)

    // Manual items definitions
    val dronesList = listOf(
        BestiaryItem(
            entityEmoji = "🤖",
            name = "CHASER STALKER",
            dangerLevel = "CRITICAL",
            behaviorName = "SMART BFS PURSUIT",
            description = "Analyzes the 2D grid matrix and computes the mathematically shortest path to intercept the player. Red LED beacons flash when locked on coordinates.",
            colorHex = "#FF3E5F"
        ),
        BestiaryItem(
            entityEmoji = "👾",
            name = "INTERCEPTOR AMBUSH",
            dangerLevel = "MODERATE",
            behaviorName = "SEMI-GREEDY PURSUIT",
            description = "Attempts to cut off the player by aiming where they are headed. Intermittently uses random steps to execute complex flank maneuvers.",
            colorHex = "#D03EFF"
        ),
        BestiaryItem(
            entityEmoji = "🛸",
            name = "SENTINEL WANDERER",
            dangerLevel = "LOW",
            behaviorName = "HIGH-SPEED PATROL",
            description = "A rapid floating drone moving at 130% standard bot speed. Patrols paths semi-randomly at intersections, relying on high frequency ticks to blind-side pilots.",
            colorHex = "#3EFFFF"
        )
    )

    val tilesList = listOf(
        Pair("🏃", "PLAYER (THE RUNNING MAN): Spawns at clean grid coordinate (1, 1). Controlled via swipe directions or on-screen mechanical cyber D-Pad."),
        Pair("⚡", "POWER ENERGY CELL: Collectible scattered in paths. Delivers +15 score points on capture. Clear all cells to advance to next level."),
        Pair("⬛", "SOLID BARRIER: Non-walkable obstacle walls spawned procedurally. Ensures full room connectivity so no items are trapped in isolation.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Upper cyber manual header
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
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Manual",
                        tint = neonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "FLIGHT MANUAL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Tactical guide to grid systems, power cells, and hostile drones",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Hostile Drone Bestiary
            item {
                Text(
                    text = "HOSTILE DRONE SPECS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = neonPink,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            items(dronesList) { bot ->
                val threatColor = when (bot.dangerLevel) {
                    "CRITICAL" -> neonPink
                    "MODERATE" -> Color(0xFFD03EFF)
                    else -> neonBlue
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, threatColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(bot.entityEmoji, fontSize = 24.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bot.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "AI BEHAVIOR: ${bot.behaviorName}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = threatColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, threatColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = bot.dangerLevel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = threatColor,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = bot.description,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Section 2: Mechanics Specs
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FIELD GRAPHICS ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = neonBlue,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        tilesList.forEach { tile ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tile.first, fontSize = 18.sp)
                                }
                                Text(
                                    text = tile.second,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
