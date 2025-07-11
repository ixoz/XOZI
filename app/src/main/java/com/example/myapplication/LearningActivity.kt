package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_activities")
data class LearningActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: "yyyy-MM-dd"
    val wordsLearned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) 