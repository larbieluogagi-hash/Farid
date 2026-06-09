package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PregnancyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: PregnancyViewModel,
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var gamerTag by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Neon laser cyberpunk colors
    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val darkBg = Color(0xFF0A0915)
    val cardBg = Color(0xFF141226)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(darkBg, Color(0xFF1B1530))
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Cyber Arcade Glowing Icon Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(16.dp, RoundedCornerShape(30.dp), ambientColor = neonPink, spotColor = neonBlue)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF1E1638)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Grid3x3,
                    contentDescription = "Grid Evasion Game",
                    tint = neonBlue,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Cyber Title Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ARCADE RETRO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = neonPink,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "GRID EVASION",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Reflexes & Tactics: Can you evade the Hunter Bots?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Registration Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = ButtonDefaults.outlinedButtonBorder.copy() 
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "NEW PILOT REGISTRATION",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = neonBlue,
                        fontFamily = FontFamily.Monospace
                    )

                    // Nickname Input in Retroware style
                    OutlinedTextField(
                        value = gamerTag,
                        onValueChange = { 
                            gamerTag = it
                            if (it.length > 15) {
                                gamerTag = it.substring(0, 15)
                            }
                            validationError = null 
                        },
                        label = { Text("Enter GamerTag", color = Color.White.copy(alpha = 0.6f)) },
                        placeholder = { Text("PlayerOne", color = Color.White.copy(alpha = 0.3f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "GamerTag",
                                tint = neonBlue
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF0C0A19),
                            unfocusedContainerColor = Color(0xFF0C0A19)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = neonPink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Interactive Instruction bullet points
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "GAME DIRECTIVES:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = neonPink,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("●", color = neonBlue)
                            Text("Swipe path or tap D-PAD controls to run on the grid.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("●", color = neonBlue)
                            Text("Gather green Energy Cells (+15 Score) to level up.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("●", color = neonBlue)
                            Text("Avoid Red & Purple chasers - they track your every move!", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Start Game/Confirm Button in cyberpunk aesthetic
            Button(
                onClick = {
                    if (gamerTag.trim().isBlank()) {
                        validationError = "PILOT TAG REQUIRED TO INITIALIZE SYSTEM"
                    } else {
                        viewModel.completeOnboarding(gamerTag)
                        onOnboardingCompleted()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonColors(
                    containerColor = neonBlue,
                    contentColor = Color.Black,
                    disabledContainerColor = neonBlue.copy(alpha = 0.5f),
                    disabledContentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = neonBlue, spotColor = neonBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "INITIALIZE FLIGHT-DECK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
