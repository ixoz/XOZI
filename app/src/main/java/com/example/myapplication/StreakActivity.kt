package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Calendar

class StreakActivity : AppCompatActivity() {
    
    private lateinit var currentStreakText: TextView
    private lateinit var longestStreakText: TextView
    private lateinit var totalLearningDaysText: TextView
    private lateinit var contributionGrid: ContributionGridView
    private lateinit var viewDetailedChartButton: MaterialButton
    private lateinit var streakManager: StreakManager
    
    // Year navigation
    private lateinit var previousYearButton: ImageButton
    private lateinit var nextYearButton: ImageButton
    private lateinit var yearLabel: TextView
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streak)
        
        streakManager = StreakManager(this)
        
        initializeViews()
        setupToolbar()
        setupClickListeners()
        loadStreakData()
    }
    
    private fun initializeViews() {
        currentStreakText = findViewById(R.id.currentStreakText)
        longestStreakText = findViewById(R.id.longestStreakText)
        totalLearningDaysText = findViewById(R.id.totalLearningDaysText)
        contributionGrid = findViewById(R.id.contributionGrid)
        viewDetailedChartButton = findViewById(R.id.viewDetailedChartButton)
        
        // Year navigation views
        previousYearButton = findViewById(R.id.previousYearButton)
        nextYearButton = findViewById(R.id.nextYearButton)
        yearLabel = findViewById(R.id.yearLabel)
        
        // Set compact mode for smaller chart in main activity
        contributionGrid.setCompactMode(true)
        
        // Initialize year label
        updateYearLabel()
    }
    
    private fun setupClickListeners() {
        viewDetailedChartButton.setOnClickListener {
            val intent = Intent(this@StreakActivity, ContributionChartActivity::class.java)
            startActivity(intent)
        }
        
        // Year navigation
        previousYearButton.setOnClickListener {
            currentYear--
            updateYearLabel()
            loadStreakDataForYear(currentYear)
        }
        
        nextYearButton.setOnClickListener {
            currentYear++
            updateYearLabel()
            loadStreakDataForYear(currentYear)
        }
    }
    
    private fun updateYearLabel() {
        yearLabel.text = currentYear.toString()
        
        // Disable next year button if we're at current year
        val currentYearNow = Calendar.getInstance().get(Calendar.YEAR)
        nextYearButton.isEnabled = currentYear < currentYearNow
        nextYearButton.alpha = if (currentYear < currentYearNow) 1.0f else 0.5f
    }
    
    private fun setupToolbar() {
        findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun loadStreakData() {
        loadStreakDataForYear(currentYear)
    }
    
    private fun loadStreakDataForYear(year: Int) {
        lifecycleScope.launch {
            // Load current streak
            streakManager.getCurrentStreak().observe(this@StreakActivity) { streak ->
                currentStreakText.text = "$streak days"
            }
            
            // Load longest streak
            streakManager.getLongestStreak().observe(this@StreakActivity) { streak ->
                longestStreakText.text = "$streak days"
            }
            
            // Load total learning days
            streakManager.getTotalLearningDays().observe(this@StreakActivity) { days ->
                totalLearningDaysText.text = "$days days"
            }
            
            // Load contribution data for specific year
            val contributionData = streakManager.getContributionDataForYear(year)
            contributionGrid.setContributionData(contributionData)
        }
    }
} 