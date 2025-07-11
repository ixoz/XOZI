package com.example.myapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StreakManager(private val context: Context) {
    
    private val database = DictionaryDatabase.getDatabase(context)
    private val learningDao = database.learningActivityDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun getCurrentStreak(): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        scope.launch {
            val learningDays = learningDao.getAllLearningDays()
            val streak = calculateCurrentStreak(learningDays)
            liveData.postValue(streak)
        }
        return liveData
    }
    
    fun getLongestStreak(): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        scope.launch {
            val learningDays = learningDao.getAllLearningDays()
            val streak = calculateLongestStreak(learningDays)
            liveData.postValue(streak)
        }
        return liveData
    }
    
    fun getTotalLearningDays(): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        scope.launch {
            val days = learningDao.getTotalLearningDays()
            liveData.postValue(days)
        }
        return liveData
    }
    
    fun getAllActivities(): Flow<List<LearningActivity>> {
        return learningDao.getAllActivities()
    }
    
    fun getActivitiesFromDate(startDate: String): Flow<List<LearningActivity>> {
        return learningDao.getActivitiesFromDate(startDate)
    }
    
    suspend fun recordLearningActivity(wordsLearned: Int = 1) {
        val today = getCurrentDate()
        val existingActivity = learningDao.getActivityForDate(today)
        
        if (existingActivity != null) {
            // Update existing activity
            val updatedActivity = existingActivity.copy(wordsLearned = existingActivity.wordsLearned + wordsLearned)
            learningDao.updateActivity(updatedActivity)
        } else {
            // Create new activity
            val newActivity = LearningActivity(date = today, wordsLearned = wordsLearned)
            learningDao.insertActivity(newActivity)
        }
    }
    

    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun calculateCurrentStreak(learningDays: List<LearningActivity>): Int {
        if (learningDays.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = getCurrentDate()
        val calendar = Calendar.getInstance()
        
        var streak = 0
        var currentDate = today
        
        while (true) {
            val hasActivity = learningDays.any { it.date == currentDate }
            if (hasActivity) {
                streak++
                // Move to previous day
                calendar.time = dateFormat.parse(currentDate) ?: break
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentDate = dateFormat.format(calendar.time)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateLongestStreak(learningDays: List<LearningActivity>): Int {
        if (learningDays.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sortedDays = learningDays.sortedBy { it.date }
        
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: String? = null
        
        for (day in sortedDays) {
            if (previousDate == null) {
                currentStreak = 1
            } else {
                val prevCalendar = Calendar.getInstance()
                val currentCalendar = Calendar.getInstance()
                
                prevCalendar.time = dateFormat.parse(previousDate) ?: continue
                currentCalendar.time = dateFormat.parse(day.date) ?: continue
                
                val daysDiff = ((currentCalendar.timeInMillis - prevCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                
                if (daysDiff == 1) {
                    currentStreak++
                } else {
                    longestStreak = maxOf(longestStreak, currentStreak)
                    currentStreak = 1
                }
            }
            previousDate = day.date
        }
        
        return maxOf(longestStreak, currentStreak)
    }
    
    suspend fun getContributionData(): List<ContributionDay> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        // Go back 365 days
        calendar.add(Calendar.DAY_OF_YEAR, -364)
        val startDate = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateString = dateFormat.format(startDate)
        
        // Get activities from database
        val activities = learningDao.getActivitiesFromDate(startDateString).first()
        
        return generateContributionDays(startDate, endDate, activities)
    }
    
    suspend fun getContributionDataForYear(year: Int): List<ContributionDay> {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0) // January 1st of the year
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        calendar.set(year, 11, 31, 23, 59, 59) // December 31st of the year
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateString = dateFormat.format(startDate)
        
        // Get activities from database for the specific year
        val activities = learningDao.getActivitiesFromDate(startDateString).first()
        
        return generateContributionDays(startDate, endDate, activities)
    }
    
    private fun generateContributionDays(startDate: Date, endDate: Date, activities: List<LearningActivity>): List<ContributionDay> {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val contributionDays = mutableListOf<ContributionDay>()
        
        while (!calendar.time.after(endDate)) {
            val dateString = dateFormat.format(calendar.time)
            val activity = activities.find { it.date == dateString }
            val wordsLearned = activity?.wordsLearned ?: 0
            
            val contributionLevel = when {
                wordsLearned == 0 -> 0
                wordsLearned <= 2 -> 1
                wordsLearned <= 5 -> 2
                wordsLearned <= 10 -> 3
                else -> 4
            }
            
            contributionDays.add(ContributionDay(dateString, wordsLearned, contributionLevel))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return contributionDays
    }
}

data class ContributionDay(
    val date: String,
    val wordsLearned: Int,
    val contributionLevel: Int // 0-4, where 0 is no activity, 4 is highest
) 