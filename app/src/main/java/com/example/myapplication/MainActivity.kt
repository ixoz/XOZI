package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var settingsButton: ImageButton
    private lateinit var adapter: DictionaryAdapter
    
    private val viewModel: DictionaryViewModel by viewModels()
    
    private val addEditEntryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val action = result.data?.getStringExtra("action")
                val entry = result.data?.getParcelableExtra<DictionaryEntry>("entry", DictionaryEntry::class.java)
                
                when (action) {
                    "delete" -> {
                        entry?.let {
                            viewModel.delete(it)
                            Toast.makeText(this, "Word deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        entry?.let {
                            if (it.id == 0L) {
                                // New entry
                                viewModel.insert(it)
                                Toast.makeText(this, "Word added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                // Updated entry
                                viewModel.update(it)
                                Toast.makeText(this, "Word updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing entry: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val wordDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val action = result.data?.getStringExtra("action")
                val entry = result.data?.getParcelableExtra<DictionaryEntry>("entry", DictionaryEntry::class.java)
                
                when (action) {
                    "delete" -> {
                        entry?.let {
                            viewModel.delete(it)
                            Toast.makeText(this, "Word deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        entry?.let {
                            // Updated entry from detail view
                            viewModel.update(it)
                            Toast.makeText(this, "Word updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing entry: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle any settings changes if needed
        // The theme change is handled automatically by the SettingsActivity
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        try {
            initializeViews()
            setupRecyclerView()
            setupObservers()
            setupClickListeners()
            
            // Check and request permissions on first launch
            checkPermissionsOnStart()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            // Try to show a simple message if everything fails
            try {
                findViewById<android.widget.TextView>(android.R.id.text1)?.text = "App initialization failed. Please restart the app."
            } catch (ignored: Exception) {
                // If even this fails, just let the app crash
            }
        }
    }
    
    private fun checkPermissionsOnStart() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs storage permission to add images to your dictionary entries. Please grant the permission when prompted.")
                .setPositiveButton("OK") { _, _ ->
                    // Permission will be requested when user tries to add an image
                }
                .show()
        }
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        fabAdd = findViewById(R.id.fabAdd)
        settingsButton = findViewById(R.id.settingsButton)
    }
    
    private fun setupRecyclerView() {
        try {
            adapter = DictionaryAdapter(
                onCardClick = { entry -> showDetailDialog(entry) },
                onEditClick = { entry -> editEntry(entry) }
            )
            
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = this@MainActivity.adapter
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up list: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupObservers() {
        viewModel.allEntries.observe(this) { entries ->
            try {
                if (::adapter.isInitialized) {
                    adapter.submitList(entries ?: emptyList())
                    updateEmptyState(entries?.isEmpty() ?: true)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error updating list: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        fabAdd.setOnClickListener {
            try {
                openAddEditActivity()
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening add activity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        settingsButton.setOnClickListener {
            try {
                openSettings()
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    val query = s.toString().trim()
                    if (query.isEmpty()) {
                        // Show all entries
                        viewModel.allEntries.observe(this@MainActivity) { entries ->
                            adapter.submitList(entries)
                            updateEmptyState(entries.isEmpty())
                        }
                    } else {
                        // Show filtered entries
                        viewModel.searchEntries(query).observe(this@MainActivity) { entries ->
                            adapter.submitList(entries)
                            updateEmptyState(entries.isEmpty())
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error searching: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }
    
    private fun openAddEditActivity() {
        try {
            val intent = Intent(this, AddEditEntryActivity::class.java)
            addEditEntryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching activity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun editEntry(entry: DictionaryEntry) {
        try {
            val intent = Intent(this, AddEditEntryActivity::class.java)
            intent.putExtra("entry", entry)
            addEditEntryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error editing entry: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openSettings() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog(entry: DictionaryEntry) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Delete Word")
                .setMessage("Are you sure you want to delete '${entry.word}'?")
                .setPositiveButton("Delete") { _, _ ->
                    try {
                        viewModel.delete(entry)
                        Toast.makeText(this, "Word deleted successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error deleting entry: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDetailDialog(entry: DictionaryEntry) {
        try {
            val intent = Intent(this, WordDetailActivity::class.java)
            intent.putExtra("entry", entry)
            wordDetailLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening detail view: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}