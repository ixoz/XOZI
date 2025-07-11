package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DictionaryEntry::class, LearningActivity::class], version = 2, exportSchema = false)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun learningActivityDao(): LearningActivityDao
    
    companion object {
        @Volatile
        private var INSTANCE: DictionaryDatabase? = null
        
        fun getDatabase(context: Context): DictionaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                )
                .fallbackToDestructiveMigration() // For development - will recreate database if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 