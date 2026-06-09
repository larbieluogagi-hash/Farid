package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameProfile::class, HighScoreEntry::class],
    version = 2, // Upgraded version to Trigger destructive migration on overwrite
    exportSchema = false
)
abstract class PregnancyDatabase : RoomDatabase() {
    abstract fun profileDao(): GameProfileDao
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        @Volatile
        private var INSTANCE: PregnancyDatabase? = null

        fun getDatabase(context: Context): PregnancyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PregnancyDatabase::class.java,
                    "evasion_game_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
