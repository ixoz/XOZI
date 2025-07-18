package com.example.myapplication

import android.app.Activity
import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import java.util.Locale

class WordDetailActivity : AppCompatActivity() {
    
    private lateinit var wordText: TextView
    private lateinit var meaningText: TextView
    private lateinit var imageCard: MaterialCardView
    private lateinit var entryImage: ImageView
    private lateinit var btnEdit: ImageButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnClose: MaterialButton
    private lateinit var playPronunciationButton: ImageButton
    
    private var currentEntry: DictionaryEntry? = null
    private var textToSpeech: TextToSpeech? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)
        
        initializeViews()
        setupClickListeners()
        initializeTextToSpeech()
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
        playPronunciationButton = findViewById(R.id.playPronunciationButton)
        
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
        
        playPronunciationButton.setOnClickListener {
            playPronunciation()
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setLanguage(Locale.US)
            }
        }
    }
    
    private fun loadWordDetails() {
        @Suppress("DEPRECATION")
currentEntry = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    intent.getParcelableExtra("entry", DictionaryEntry::class.java)
} else {
    intent.getParcelableExtra("entry")
}
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
    
    private fun playPronunciation() {
        currentEntry?.let { entry ->
            val textToSpeak = entry.pronunciation ?: entry.word
            textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "pronunciation")
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
            @Suppress("DEPRECATION")
val entry = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    data?.getParcelableExtra("entry", DictionaryEntry::class.java)
} else {
    data?.getParcelableExtra("entry")
}

            
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
    
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
    
    companion object {
        private const val REQUEST_EDIT_ENTRY = 1001
    }
} 