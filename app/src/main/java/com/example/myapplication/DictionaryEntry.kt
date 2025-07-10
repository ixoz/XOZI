package com.example.myapplication

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "dictionary_entries")
data class DictionaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val meaning: String,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable 