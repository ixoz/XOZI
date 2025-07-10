package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary_entries ORDER BY word ASC")
    fun getAllEntries(): LiveData<List<DictionaryEntry>>
    
    @Query("SELECT * FROM dictionary_entries ORDER BY word ASC")
    suspend fun getAllEntriesSync(): List<DictionaryEntry>
    
    @Query("SELECT * FROM dictionary_entries WHERE word LIKE :searchQuery OR meaning LIKE :searchQuery ORDER BY word ASC")
    fun searchEntries(searchQuery: String): LiveData<List<DictionaryEntry>>
    
    @Insert
    suspend fun insertEntry(entry: DictionaryEntry)
    
    @Update
    suspend fun updateEntry(entry: DictionaryEntry)
    
    @Delete
    suspend fun deleteEntry(entry: DictionaryEntry)
    
    @Query("DELETE FROM dictionary_entries")
    suspend fun deleteAllEntries()
    
    @Query("SELECT * FROM dictionary_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): DictionaryEntry?
} 