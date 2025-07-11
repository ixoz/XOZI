package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class ContributionGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var contributionDays: List<ContributionDay> = emptyList()
    private var cellSize = 0f
    private var cellSpacing = 4f // Slightly more spacing for bigger boxes
    private var columns = 14 // Default: 14 columns x 26 rows = 364 days (close to 365)
    private var rows = 26
    private var isCompactMode = false // New parameter for smaller size
    
    fun setContributionData(data: List<ContributionDay>) {
        contributionDays = data
        invalidate()
    }
    
    fun setCompactMode(compact: Boolean) {
        isCompactMode = compact
        if (compact) {
            columns = 31 // 31 columns for compact mode
            rows = 12 // 31 x 12 = 372 days (close to 365)
        } else {
            columns = 14 // 14 columns for detailed mode
            rows = 26 // 14 x 26 = 364 days
        }
        requestLayout()
        invalidate()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        
        // Adjust spacing and minimum size based on mode
        val spacing = if (isCompactMode) 2f else 3f
        val minCellSize = if (isCompactMode) 8f else 12f // Smaller boxes for detailed mode
        
        // Calculate cell size to fit all columns properly with consistent spacing
        val calculatedCellSize = if (width > 0) {
            ((width - (columns - 1) * spacing) / columns).toFloat()
        } else {
            // For wrap_content, use a reasonable default size
            if (isCompactMode) 8f else 12f
        }
        
        // Use consistent cell size for all boxes
        cellSize = maxOf(calculatedCellSize, minCellSize)
        cellSpacing = spacing
        
        // Calculate height based on cell size to ensure all rows fit
        val desiredHeight = (rows * (cellSize + cellSpacing) - cellSpacing).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        
        // For wrap_content width, calculate the actual width needed
        val actualWidth = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || 
                             MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            (columns * (cellSize + cellSpacing) - cellSpacing).toInt()
        } else {
            width
        }
        
        setMeasuredDimension(actualWidth, height)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (contributionDays.isEmpty()) return
        
        // Take the last days based on columns x rows
        val totalDays = columns * rows
        val displayDays = contributionDays.takeLast(totalDays)
        
        for (i in displayDays.indices) {
            val row = i / columns
            val col = i % columns
            
            val x = col * (cellSize + cellSpacing)
            val y = row * (cellSize + cellSpacing)
            
            val day = displayDays[i]
            val color = getContributionColor(day.contributionLevel)
            
            paint.color = color
            
            // Draw rounded rectangle with consistent curves
            val rect = RectF(x, y, x + cellSize, y + cellSize)
            val cornerRadius = if (isCompactMode) 4f else 6f
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }
    
    private fun getContributionColor(level: Int): Int {
        return when (level) {
            0 -> Color.parseColor("#EBEDF0") // No activity
            1 -> Color.parseColor("#9BE9A8") // Light green
            2 -> Color.parseColor("#40C463") // Medium green
            3 -> Color.parseColor("#30A14E") // Dark green
            4 -> Color.parseColor("#216E39") // Darkest green
            else -> Color.parseColor("#EBEDF0")
        }
    }
} 