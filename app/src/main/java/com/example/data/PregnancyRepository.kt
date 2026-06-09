package com.example.data

import kotlinx.coroutines.flow.Flow

class PregnancyRepository(private val database: PregnancyDatabase) {

    private val profileDao = database.profileDao()
    private val highScoreDao = database.highScoreDao()

    // Profile Operations
    val profile: Flow<GameProfile?> = profileDao.getProfile()

    suspend fun getProfileDirect(): GameProfile? {
        return profileDao.getProfileDirect()
    }

    suspend fun saveProfile(profile: GameProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }

    // High Score Operations
    val highScores: Flow<List<HighScoreEntry>> = highScoreDao.getAllHighScores()

    suspend fun addHighScore(entry: HighScoreEntry) {
        highScoreDao.insertHighScore(entry)
    }

    suspend fun clearAllHighScores() {
        highScoreDao.clearAllHighScores()
    }
}
