package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File

class WordDetailActivity : AppCompatActivity() {
    
    private lateinit var wordText: TextView
    private lateinit var meaningText: TextView
    private lateinit var imageCard: MaterialCardView
    private lateinit var entryImage: ImageView
    private lateinit var btnEdit: ImageButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnClose: MaterialButton
    
    private var currentEntry: DictionaryEntry? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)
        
        initializeViews()
        setupClickListeners()
        loadWordDetails()
    }
    
    private fun initializeViews() {
        wordText = findViewById(R.id.detailWordText)
        meaningText = findViewById(R.id.detailMeaningText)
        imageCard = findViewById(R.id.detailImageCard)
        entryImage = findViewById(R.id.detailEntryImage)
        btnEdit = findViewById(R.id.btnEditDetail)
        btnDelete = findViewById(R.id.btnDeleteDetail)
        btnClose = findViewById(R.id.btnCloseDetail)
        
        // Setup toolbar
        findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupClickListeners() {
        btnEdit.setOnClickListener {
            currentEntry?.let { entry ->
                val intent = Intent(this, AddEditEntryActivity::class.java)
                intent.putExtra("entry", entry)
                startActivityForResult(intent, REQUEST_EDIT_ENTRY)
            }
        }
        
        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
        
        btnClose.setOnClickListener {
            finish()
        }
    }
    
    private fun loadWordDetails() {
        currentEntry = intent.getParcelableExtra("entry", DictionaryEntry::class.java)
        currentEntry?.let { entry ->
            wordText.text = entry.word
            meaningText.text = entry.meaning
            
            // Handle image display
            if (!entry.imagePath.isNullOrEmpty()) {
                imageCard.visibility = View.VISIBLE
                Glide.with(this)
                    .load(File(entry.imagePath))
                    .placeholder(R.drawable.image_background)
                    .error(R.drawable.image_background)
                    .into(entryImage)
            } else {
                imageCard.visibility = View.GONE
            }
        }
    }
    
    private fun showDeleteConfirmation() {
        currentEntry?.let { entry ->
            AlertDialog.Builder(this)
                .setTitle("Delete Word")
                .setMessage("Are you sure you want to delete '${entry.word}'?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteEntry(entry)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun deleteEntry(entry: DictionaryEntry) {
        val intent = Intent()
        intent.putExtra("action", "delete")
        intent.putExtra("entry", entry)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_EDIT_ENTRY && resultCode == Activity.RESULT_OK) {
            val action = data?.getStringExtra("action")
            val entry = data?.getParcelableExtra<DictionaryEntry>("entry", DictionaryEntry::class.java)
            
            when (action) {
                "delete" -> {
                    entry?.let {
                        val intent = Intent()
                        intent.putExtra("action", "delete")
                        intent.putExtra("entry", it)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
                else -> {
                    entry?.let {
                        // Update the current entry and refresh the view
                        currentEntry = it
                        loadWordDetails()
                        
                        // Pass the update back to MainActivity
                        val intent = Intent()
                        intent.putExtra("entry", it)
                        setResult(Activity.RESULT_OK, intent)
                    }
                }
            }
        }
    }
    
    companion object {
        private const val REQUEST_EDIT_ENTRY = 1001
    }
} 