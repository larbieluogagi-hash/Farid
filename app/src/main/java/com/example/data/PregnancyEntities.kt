package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profile")
data class GameProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "Player 1",
    val onboardingCompleted: Boolean = false,
    val selectedTheme: String = "NEON_ARCADE" // NEON_ARCADE, RETRO_TERMINAL, CYBERPUNK
)

@Entity(tableName = "high_score_entries")
data class HighScoreEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val score: Int,
    val survivalTimeSeconds: Int,
    val difficulty: String, // "EASY", "NORMAL", "HARD"
    val timestamp: Long = System.currentTimeMillis()
)
