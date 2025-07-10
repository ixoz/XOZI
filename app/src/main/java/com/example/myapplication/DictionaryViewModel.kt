package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: DictionaryRepository
    val allEntries: LiveData<List<DictionaryEntry>>
    
    init {
        val dao = DictionaryDatabase.getDatabase(application).dictionaryDao()
        repository = DictionaryRepository(dao)
        allEntries = repository.allEntries
    }
    
    fun insert(entry: DictionaryEntry) = viewModelScope.launch {
        repository.insert(entry)
    }
    
    fun update(entry: DictionaryEntry) = viewModelScope.launch {
        repository.update(entry)
    }
    
    fun delete(entry: DictionaryEntry) = viewModelScope.launch {
        repository.delete(entry)
    }
    
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
    
    fun searchEntries(query: String): LiveData<List<DictionaryEntry>> {
        return repository.searchEntries(query)
    }
    
    suspend fun getEntryById(id: Long): DictionaryEntry? {
        return repository.getEntryById(id)
    }
} 