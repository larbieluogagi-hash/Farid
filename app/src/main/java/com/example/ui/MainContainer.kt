package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PregnancyViewModel

enum class ScreenTab {
    DASHBOARD, FOOD_SAFETY, TOOLS, PROFILE
}

@Composable
fun MainContainer(
    viewModel: PregnancyViewModel,
    modifier: Modifier = Modifier
) {
    val isOnboarded by viewModel.isOnboardingCompleted.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    val neonBlue = Color(0xFF00F0FF)
    val neonPink = Color(0xFFFF007F)
    val selectionIndicatorColor = Color(0xFF1E1438)

    if (!isOnboarded || profileState == null) {
        OnboardingScreen(
            viewModel = viewModel,
            onOnboardingCompleted = {}
        )
    } else {
        var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
        val currentProfile = profileState!!

        Scaffold(
            bottomBar = {
                NavigationBar(
                    // Safe-area-compliant bottom inset padding
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp,
                    containerColor = Color(0xFF110E24) // Deep Space Arcade background
                ) {
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DASHBOARD,
                        onClick = { currentTab = ScreenTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.DASHBOARD) Icons.Default.SportsEsports else Icons.Outlined.SportsEsports,
                                contentDescription = "Console Dashboard"
                            )
                        },
                        label = { Text("Game", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = neonBlue,
                            selectedTextColor = neonBlue,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f),
                            indicatorColor = selectionIndicatorColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.FOOD_SAFETY,
                        onClick = { currentTab = ScreenTab.FOOD_SAFETY },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.FOOD_SAFETY) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Flight Manual"
                            )
                        },
                        label = { Text("Manual", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = neonBlue,
                            selectedTextColor = neonBlue,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f),
                            indicatorColor = selectionIndicatorColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.TOOLS,
                        onClick = { currentTab = ScreenTab.TOOLS },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.TOOLS) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "Trophies Leaderboard"
                            )
                        },
                        label = { Text("Rankings", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = neonBlue,
                            selectedTextColor = neonBlue,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f),
                            indicatorColor = selectionIndicatorColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PROFILE,
                        onClick = { currentTab = ScreenTab.PROFILE },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "Pilot Settings"
                            )
                        },
                        label = { Text("Pilot", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = neonBlue,
                            selectedTextColor = neonBlue,
                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            unselectedTextColor = Color.White.copy(alpha = 0.4f),
                            indicatorColor = selectionIndicatorColor
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        profile = currentProfile,
                        onNavigateToTools = { currentTab = ScreenTab.TOOLS }
                    )
                    ScreenTab.FOOD_SAFETY -> FoodSafetyScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.TOOLS -> ToolsScreen(
                        viewModel = viewModel
                    )
                    ScreenTab.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        profile = currentProfile
                    )
                }
            }
        }
    }
}
