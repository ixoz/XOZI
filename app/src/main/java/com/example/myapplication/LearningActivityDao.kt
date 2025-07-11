package com.example.myapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningActivityDao {
    
    @Query("SELECT * FROM learning_activities WHERE date = :date")
    suspend fun getActivityForDate(date: String): LearningActivity?
    
    @Query("SELECT * FROM learning_activities ORDER BY date DESC LIMIT 365")
    fun getAllActivities(): Flow<List<LearningActivity>>
    
    @Query("SELECT * FROM learning_activities WHERE date >= :startDate ORDER BY date ASC")
    fun getActivitiesFromDate(startDate: String): Flow<List<LearningActivity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LearningActivity)
    
    @Update
    suspend fun updateActivity(activity: LearningActivity)
    
    @Query("SELECT COUNT(*) FROM learning_activities WHERE wordsLearned > 0")
    suspend fun getTotalLearningDays(): Int
    
    @Query("SELECT * FROM learning_activities WHERE wordsLearned > 0 ORDER BY date DESC")
    suspend fun getAllLearningDays(): List<LearningActivity>
} 