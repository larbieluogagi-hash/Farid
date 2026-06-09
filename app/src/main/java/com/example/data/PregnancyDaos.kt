package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<GameProfile?>

    @Query("SELECT * FROM game_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): GameProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: GameProfile)
}

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_score_entries ORDER BY score DESC, survivalTimeSeconds DESC LIMIT 50")
    fun getAllHighScores(): Flow<List<HighScoreEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(entry: HighScoreEntry)

    @Delete
    suspend fun deleteHighScore(entry: HighScoreEntry)

    @Query("DELETE FROM high_score_entries")
    suspend fun clearAllHighScores()
}
