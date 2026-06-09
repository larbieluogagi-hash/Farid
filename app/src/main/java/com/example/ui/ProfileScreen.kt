package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameProfile
import com.example.viewmodel.PregnancyViewModel

@Composable
fun ProfileScreen(
    viewModel: PregnancyViewModel,
    profile: GameProfile,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(profile.username) }

    val scores by viewModel.highScores.collectAsState()

    // Dialog state for resetting application state data
    var showResetDialog by remember { mutableStateOf(false) }

    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF39FF14)
    val darkBg = Color(0xFF0A0915)
    val cardBg = Color(0xFF141226)

    // Calculate dynamic aggregates
    val totalRuns = scores.size
    val highestScore = if (scores.isNotEmpty()) scores.maxOf { it.score } else 0
    val totalSurvivalTime = scores.sumOf { it.survivalTimeSeconds }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "PURGE FLIGHT SYSTEM?",
                    fontWeight = FontWeight.Bold,
                    color = neonPink,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "This will permanently wipe your pilot registry, reset your customized GamerTag, and delete ALL local SQL High Scores history. This terminal action cannot be reverted.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPink)
                ) {
                    Text("PURGE ALL DATA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("ABORT PURGE", color = neonBlue, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // App bar
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = neonPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "PILOT PROFILE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Configure flight transmitter and examine system telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Profile details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Card with Nickname editor
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar glow structure
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(neonBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = neonBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Interactive editing row
                    if (isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Update Tag", color = Color.White.copy(alpha = 0.6f)) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonBlue,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedContainerColor = Color(0xFF0C0A19),
                                    unfocusedContainerColor = Color(0xFF0C0A19)
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (editedName.isNotBlank()) {
                                        viewModel.updateProfileName(editedName)
                                    }
                                    isEditingName = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save edit",
                                    tint = neonGreen
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable { isEditingName = true }
                        ) {
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit name",
                                tint = neonBlue.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "RANK STATUS: SHIELD LEVEL 1",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = neonPink,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Game aggregates telemetry statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FLIGHT TELEMETRY DATA",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = neonBlue,
                        fontFamily = FontFamily.Monospace
                    )

                    // Param 1: Total Simulator Runs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Total Runs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "$totalRuns",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Param 2: Personal Best
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Personal Best",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "%04d PTS".format(highestScore),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = neonGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Param 3: Total Survived Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = neonPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Accumulated Time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "%d M %s S".format(totalSurvivalTime / 60, totalSurvivalTime % 60),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // System Management
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, neonPink.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TERMINAL CONTROL PANEL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = neonPink,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "If you want to clear your high scores profile completely and register under a new Pilot GamerTag, perform a hard clear below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonPink.copy(alpha = 0.12f),
                            contentColor = neonPink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Purge data",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PURGE ALL FLIGHT LOGS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
