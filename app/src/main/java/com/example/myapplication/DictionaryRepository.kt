package com.example.myapplication

import androidx.lifecycle.LiveData

class DictionaryRepository(private val dictionaryDao: DictionaryDao) {
    
    val allEntries: LiveData<List<DictionaryEntry>> = dictionaryDao.getAllEntries()
    
    suspend fun insert(entry: DictionaryEntry) {
        dictionaryDao.insertEntry(entry)
    }
    
    suspend fun update(entry: DictionaryEntry) {
        dictionaryDao.updateEntry(entry)
    }
    
    suspend fun delete(entry: DictionaryEntry) {
        dictionaryDao.deleteEntry(entry)
    }
    
    suspend fun deleteAll() {
        dictionaryDao.deleteAllEntries()
    }
    
    fun searchEntries(query: String): LiveData<List<DictionaryEntry>> {
        return dictionaryDao.searchEntries("%$query%")
    }
    
    suspend fun getEntryById(id: Long): DictionaryEntry? {
        return dictionaryDao.getEntryById(id)
    }
} 