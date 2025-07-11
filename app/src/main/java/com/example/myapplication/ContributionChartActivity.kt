package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class ContributionChartActivity : AppCompatActivity() {
    
    private lateinit var contributionGrid: ContributionGridView
    private lateinit var streakManager: StreakManager
    
    // Year navigation
    private lateinit var previousYearButton: ImageButton
    private lateinit var nextYearButton: ImageButton
    private lateinit var yearLabel: TextView
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contribution_chart)
        
        streakManager = StreakManager(this)
        
        initializeViews()
        setupToolbar()
        setupClickListeners()
        loadContributionData()
    }
    
    private fun initializeViews() {
        contributionGrid = findViewById(R.id.contributionGrid)
        
        // Year navigation views
        previousYearButton = findViewById(R.id.previousYearButton)
        nextYearButton = findViewById(R.id.nextYearButton)
        yearLabel = findViewById(R.id.yearLabel)
        
        // Initialize year label
        updateYearLabel()
    }
    
    private fun setupClickListeners() {
        // Year navigation
        previousYearButton.setOnClickListener {
            currentYear--
            updateYearLabel()
            loadContributionDataForYear(currentYear)
        }
        
        nextYearButton.setOnClickListener {
            currentYear++
            updateYearLabel()
            loadContributionDataForYear(currentYear)
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
    
    private fun loadContributionData() {
        loadContributionDataForYear(currentYear)
    }
    
    private fun loadContributionDataForYear(year: Int) {
        lifecycleScope.launch {
            // Load contribution data for specific year
            val contributionData = streakManager.getContributionDataForYear(year)
            contributionGrid.setContributionData(contributionData)
        }
    }
} 