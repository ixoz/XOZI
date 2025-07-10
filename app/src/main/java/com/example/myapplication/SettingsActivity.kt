package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.io.File

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var darkThemeSwitch: SwitchMaterial
    private lateinit var appVersionText: TextView
    private lateinit var exportCard: MaterialCardView
    private lateinit var importCard: MaterialCardView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backupManager: BackupManager
    private val viewModel: DictionaryViewModel by viewModels()
    
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importBackup(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        backupManager = BackupManager(this)
        
        initializeViews()
        setupClickListeners()
        loadSettings()
    }
    
    private fun initializeViews() {
        darkThemeSwitch = findViewById(R.id.darkThemeSwitch)
        appVersionText = findViewById(R.id.appVersionText)
        exportCard = findViewById(R.id.exportCard)
        importCard = findViewById(R.id.importCard)
        
        // Setup toolbar
        findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupClickListeners() {
        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveDarkThemePreference(isChecked)
            applyDarkTheme(isChecked)
        }
        
        exportCard.setOnClickListener {
            exportBackup()
        }
        
        importCard.setOnClickListener {
            selectBackupFile()
        }
    }
    
    private fun loadSettings() {
        // Load dark theme preference
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        darkThemeSwitch.isChecked = isDarkTheme
        
        // Set app version
        appVersionText.text = "1.0.0"
    }
    
    private fun saveDarkThemePreference(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("dark_theme", isDark).apply()
    }
    
    private fun applyDarkTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun exportBackup() {
        lifecycleScope.launch {
            try {
                // Show loading dialog
                val loadingDialog = AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Creating Backup")
                    .setMessage("Please wait while we create your backup...")
                    .setCancelable(false)
                    .create()
                loadingDialog.show()
                
                // Get all entries directly from database
                val dao = DictionaryDatabase.getDatabase(this@SettingsActivity).dictionaryDao()
                val entries = dao.getAllEntriesSync()
                
                if (entries.isEmpty()) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@SettingsActivity, "No words to backup", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Create backup
                val result = backupManager.exportBackup(entries)
                
                loadingDialog.dismiss()
                
                if (result.isSuccess) {
                    val backupPath = result.getOrNull()
                    val backupFile = File(backupPath ?: "")
                    
                    // Add detailed logging
                    android.util.Log.d("SettingsActivity", "Backup file path: ${backupFile.absolutePath}")
                    android.util.Log.d("SettingsActivity", "Backup file exists: ${backupFile.exists()}")
                    android.util.Log.d("SettingsActivity", "Backup file size: ${backupFile.length()} bytes")
                    
                    // Read and log the first few lines of the backup file
                    try {
                        val backupContent = backupFile.readText()
                        android.util.Log.d("SettingsActivity", "Backup content (first 200 chars): ${backupContent.take(200)}")
                    } catch (e: Exception) {
                        android.util.Log.e("SettingsActivity", "Error reading backup file: ${e.message}")
                    }
                    
                    AlertDialog.Builder(this@SettingsActivity)
                        .setTitle("Backup Created")
                        .setMessage("Backup saved successfully!\n\nLocation: Downloads/DictionaryBackups/\n\nFile: ${backupFile.name}\nSize: ${backupFile.length()} bytes\n\nFull path: ${backupFile.absolutePath}")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val error = result.exceptionOrNull()
                    Toast.makeText(this@SettingsActivity, "Backup failed: ${error?.message}", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error creating backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun selectBackupFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/zip"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        importLauncher.launch(intent)
    }
    
    private fun importBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Show loading dialog
                val loadingDialog = AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Importing Backup")
                    .setMessage("Please wait while we import your backup...")
                    .setCancelable(false)
                    .create()
                loadingDialog.show()
                
                // Import backup
                val result = backupManager.importBackup(uri)
                
                loadingDialog.dismiss()
                
                if (result.isSuccess) {
                    val entries = result.getOrNull() ?: emptyList()
                    
                    if (entries.isNotEmpty()) {
                        // Show confirmation dialog
                        AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("Import Backup")
                            .setMessage("This will import ${entries.size} words. This action cannot be undone. Do you want to continue?")
                            .setPositiveButton("Import") { _, _ ->
                                importEntries(entries)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "No valid entries found in backup", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Toast.makeText(this@SettingsActivity, "Import failed: ${error?.message}", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error importing backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun importEntries(entries: List<DictionaryEntry>) {
        lifecycleScope.launch {
            try {
                // Clear existing entries and import new ones
                viewModel.deleteAll()
                
                entries.forEach { entry ->
                    // Reset ID to 0 so it's treated as a new entry
                    val newEntry = entry.copy(id = 0)
                    viewModel.insert(newEntry)
                }
                
                Toast.makeText(this@SettingsActivity, "Successfully imported ${entries.size} words", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error importing entries: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
} 