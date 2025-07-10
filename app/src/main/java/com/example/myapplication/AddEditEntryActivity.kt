package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddEditEntryActivity : AppCompatActivity() {

    private lateinit var wordEditText: TextInputEditText
    private lateinit var meaningEditText: TextInputEditText
    private lateinit var selectedImage: ImageView
    private lateinit var imagePlaceholder: LinearLayout
    private lateinit var imageCard: MaterialCardView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var selectedImagePath: String? = null
    private var editingEntry: DictionaryEntry? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied. Please grant storage permission in Settings.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_entry)

        initializeViews()
        setupClickListeners()
        loadEntryForEdit()
    }

    private fun initializeViews() {
        wordEditText = findViewById(R.id.wordEditText)
        meaningEditText = findViewById(R.id.meaningEditText)
        selectedImage = findViewById(R.id.selectedImage)
        imagePlaceholder = findViewById(R.id.imagePlaceholder)
        imageCard = findViewById(R.id.imageCard)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)

        // Setup toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
            title = "Add New Word"
        }
    }

    private fun setupClickListeners() {
        imageCard.setOnClickListener {
            checkPermissionAndPickImage()
        }

        btnSave.setOnClickListener {
            saveEntry()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadEntryForEdit() {
    try {
        @Suppress("DEPRECATION")
        editingEntry = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("entry", DictionaryEntry::class.java)
        } else {
            intent.getParcelableExtra("entry")
        }

        if (editingEntry != null) {
            val entry = editingEntry!!
            wordEditText.setText(entry.word)
            meaningEditText.setText(entry.meaning)
            selectedImagePath = entry.imagePath

            if (!entry.imagePath.isNullOrEmpty()) {
                displaySelectedImage(entry.imagePath)
            }

            // Update toolbar title and show delete button
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).title = "Edit Word"
            btnDelete.visibility = View.VISIBLE
        } else {
            // Add mode
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).title = "Add New Word"
            btnDelete.visibility = View.GONE
        }
    } catch (e: Exception) {
        Toast.makeText(this, "Error loading entry: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}


    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        try {
            getContent.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(filesDir, "dictionary_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            selectedImagePath = file.absolutePath
            displaySelectedImage(selectedImagePath!!)
            Toast.makeText(this, "Image added successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySelectedImage(imagePath: String) {
        selectedImage.visibility = View.VISIBLE
        imagePlaceholder.visibility = View.GONE

        com.bumptech.glide.Glide.with(this)
            .load(File(imagePath))
            .into(selectedImage)
    }

    private fun saveEntry() {
        val word = wordEditText.text.toString().trim()
        val meaning = meaningEditText.text.toString().trim()

        if (word.isEmpty()) {
            wordEditText.error = "Please enter a word"
            return
        }

        if (meaning.isEmpty()) {
            meaningEditText.error = "Please enter a meaning"
            return
        }

        val entry = editingEntry?.copy(
            word = word,
            meaning = meaning,
            imagePath = selectedImagePath
        ) ?: DictionaryEntry(
            word = word,
            meaning = meaning,
            imagePath = selectedImagePath
        )

        val resultIntent = Intent()
        resultIntent.putExtra("entry", entry)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun showDeleteConfirmation() {
        editingEntry?.let { entry ->
            androidx.appcompat.app.AlertDialog.Builder(this)
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
        val resultIntent = Intent()
        resultIntent.putExtra("action", "delete")
        resultIntent.putExtra("entry", entry)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
