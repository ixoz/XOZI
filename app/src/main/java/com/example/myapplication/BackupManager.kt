package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    suspend fun exportBackup(entries: List<DictionaryEntry>): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("BackupManager", "Starting ZIP backup export with ${entries.size} entries")
            
            // Create timestamp for backup
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val backupName = "dictionary_backup_$timestamp"
            
            // Create temporary directory for backup contents
            val tempDir = File(context.cacheDir, backupName)
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()
            
            // Create images folder
            val imagesDir = File(tempDir, "images")
            imagesDir.mkdirs()
            
            // Create data file
            val dataFile = File(tempDir, "dictionary_data.txt")
            val dataWriter = BufferedWriter(FileWriter(dataFile))
            
            // Write header
            dataWriter.write("Dictionary Backup\n")
            dataWriter.write("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
            dataWriter.write("Total entries: ${entries.size}\n")
            dataWriter.write("=".repeat(50) + "\n\n")
            
            // Process entries and copy images
            entries.forEach { entry ->
                dataWriter.write("ID: ${entry.id}\n")
                dataWriter.write("Word: ${entry.word}\n")
                dataWriter.write("Meaning: ${entry.meaning}\n")
                
                // Handle image
                val imageFileName = entry.imagePath?.let { path ->
                    val sourceFile = File(path)
                    if (sourceFile.exists()) {
                        // Copy image to images folder
                        val destFile = File(imagesDir, sourceFile.name)
                        sourceFile.copyTo(destFile, overwrite = true)
                        "images/${sourceFile.name}"
                    } else {
                        "None"
                    }
                } ?: "None"
                
                dataWriter.write("Image: $imageFileName\n")
                dataWriter.write("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entry.createdAt))}\n")
                dataWriter.write("-".repeat(30) + "\n\n")
            }
            dataWriter.close()
            
            // Create ZIP file
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadsDir, "DictionaryBackups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val zipFile = File(backupDir, "$backupName.zip")
            createZipFile(tempDir, zipFile)
            
            // Clean up temp directory
            tempDir.deleteRecursively()
            
            Log.d("BackupManager", "ZIP backup created successfully: ${zipFile.absolutePath}")
            Log.d("BackupManager", "ZIP file size: ${zipFile.length()} bytes")
            
            Result.success(zipFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e("BackupManager", "Error creating ZIP backup: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun createZipFile(sourceDir: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipDir(sourceDir, sourceDir, zipOut)
        }
    }
    
    private fun zipDir(sourceDir: File, dir: File, zipOut: ZipOutputStream) {
        val files = dir.listFiles() ?: return
        
        for (file in files) {
            if (file.isDirectory) {
                zipDir(sourceDir, file, zipOut)
            } else {
                val entry = ZipEntry(file.relativeTo(sourceDir).path)
                zipOut.putNextEntry(entry)
                
                FileInputStream(file).use { input ->
                    input.copyTo(zipOut)
                }
                
                zipOut.closeEntry()
            }
        }
    }
    
    suspend fun importBackup(uri: Uri): Result<List<DictionaryEntry>> = withContext(Dispatchers.IO) {
        try {
            // Read ZIP file from URI
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return@withContext Result.failure(IOException("Could not read backup file"))
            }
            
            // Create temporary directory for extraction
            val tempDir = File(context.cacheDir, "import_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()
            
            // Extract ZIP file
            extractZipFile(inputStream, tempDir)
            
            // Read data file
            val dataFile = File(tempDir, "dictionary_data.txt")
            if (!dataFile.exists()) {
                return@withContext Result.failure(IOException("Data file not found in backup"))
            }
            
            val entries = parseBackupData(dataFile, tempDir)
            
            // Clean up temp directory
            tempDir.deleteRecursively()
            
            if (entries.isEmpty()) {
                return@withContext Result.failure(IOException("No valid entries found in backup"))
            }
            
            Log.d("BackupManager", "ZIP backup imported successfully: ${entries.size} entries")
            Result.success(entries)
            
        } catch (e: Exception) {
            Log.e("BackupManager", "Error importing ZIP backup: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun extractZipFile(inputStream: InputStream, destDir: File) {
        val buffer = ByteArray(1024)
        val zipInputStream = java.util.zip.ZipInputStream(inputStream)
        
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val file = File(destDir, entry.name)
            
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                // Create parent directories if they don't exist
                file.parentFile?.mkdirs()
                
                // Write file
                FileOutputStream(file).use { output ->
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        output.write(buffer, 0, len)
                    }
                }
            }
            
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }
    
    private fun parseBackupData(dataFile: File, baseDir: File): List<DictionaryEntry> {
        val entries = mutableListOf<DictionaryEntry>()
        val reader = BufferedReader(FileReader(dataFile))
        
        var currentEntry: MutableMap<String, String>? = null
        
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            when {
                line?.startsWith("ID: ") == true -> {
                    currentEntry = mutableMapOf()
                    currentEntry["id"] = line.substring(4)
                }
                line?.startsWith("Word: ") == true -> {
                    currentEntry?.put("word", line.substring(6))
                }
                line?.startsWith("Meaning: ") == true -> {
                    currentEntry?.put("meaning", line.substring(9))
                }
                line?.startsWith("Image: ") == true -> {
                    val imagePath = line.substring(7)
                    currentEntry?.put("imagePath", if (imagePath == "None") "" else imagePath)
                }
                line?.startsWith("Created: ") == true -> {
                    currentEntry?.put("createdAt", line.substring(9))
                }
                line?.startsWith("-") == true && currentEntry != null -> {
                    // End of entry, create DictionaryEntry
                    try {
                        val id = currentEntry["id"]?.toLongOrNull() ?: 0L
                        val word = currentEntry["word"] ?: ""
                        val meaning = currentEntry["meaning"] ?: ""
                        val imagePath = currentEntry["imagePath"]?.takeIf { it.isNotEmpty() }?.let { path ->
                            if (path.startsWith("images/")) {
                                // Copy image from extracted ZIP to app's internal storage
                                val imageFile = File(baseDir, path)
                                if (imageFile.exists()) {
                                    val appImageDir = File(context.filesDir, "dictionary_images")
                                    appImageDir.mkdirs()
                                    val destFile = File(appImageDir, imageFile.name)
                                    imageFile.copyTo(destFile, overwrite = true)
                                    destFile.absolutePath
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                        val createdAt = currentEntry["createdAt"]?.let { 
                            try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                        } ?: System.currentTimeMillis()
                        
                        if (word.isNotEmpty() && meaning.isNotEmpty()) {
                            entries.add(DictionaryEntry(id, word, meaning, imagePath, createdAt))
                        }
                    } catch (e: Exception) {
                        Log.e("BackupManager", "Error parsing entry: $currentEntry", e)
                    }
                    currentEntry = null
                }
            }
        }
        reader.close()
        
        return entries
    }
    
    fun getBackupFiles(): List<File> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val backupDir = File(downloadsDir, "DictionaryBackups")
        return backupDir.listFiles()?.filter { it.name.endsWith(".zip") }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun listBackupDirectoryContents(): String {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadsDir, "DictionaryBackups")
            val files = backupDir.listFiles()
            
            if (files == null) {
                "Directory is null or doesn't exist"
            } else {
                "Directory: ${backupDir.absolutePath}\n" +
                "Exists: ${backupDir.exists()}\n" +
                "Can write: ${backupDir.canWrite()}\n" +
                "Files: ${files.size}\n" +
                files.joinToString("\n") { "${it.name} (${it.length()} bytes)" }
            }
        } catch (e: Exception) {
            "Error listing directory: ${e.message}"
        }
    }
    
    fun deleteBackupFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e("BackupManager", "Error deleting backup file: ${e.message}", e)
            false
        }
    }
    
    fun testFileCreation(): String {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadsDir, "DictionaryBackups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val testFile = File(backupDir, "test_file.txt")
            testFile.writeText("Test content")
            
            val exists = testFile.exists()
            val size = testFile.length()
            val canRead = testFile.canRead()
            
            "Test file created: $exists, Size: $size bytes, Can read: $canRead"
        } catch (e: Exception) {
            "Error creating test file: ${e.message}"
        }
    }
} 