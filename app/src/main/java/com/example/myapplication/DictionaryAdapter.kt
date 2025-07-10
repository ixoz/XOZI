package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class DictionaryAdapter(
    private val onCardClick: (DictionaryEntry) -> Unit,
    private val onEditClick: (DictionaryEntry) -> Unit
) : ListAdapter<DictionaryEntry, DictionaryAdapter.ViewHolder>(DictionaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dictionary_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView = itemView.findViewById(R.id.wordText)
        private val meaningText: TextView = itemView.findViewById(R.id.meaningText)
        private val entryImage: ImageView = itemView.findViewById(R.id.entryImage)
        private val imageContainer: FrameLayout = itemView.findViewById(R.id.imageContainer)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)

        fun bind(entry: DictionaryEntry) {
            try {
                wordText.text = entry.word
                meaningText.text = entry.meaning

                // Handle image display
                if (!entry.imagePath.isNullOrEmpty()) {
                    imageContainer.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(File(entry.imagePath))
                        .placeholder(R.drawable.image_background)
                        .error(R.drawable.image_background)
                        .into(entryImage)
                } else {
                    imageContainer.visibility = View.GONE
                }

                // Card click for detailed view
                itemView.setOnClickListener { onCardClick(entry) }
                
                // Edit icon click
                editIcon.setOnClickListener { onEditClick(entry) }
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("DictionaryAdapter", "Error binding entry: ${e.message}", e)
            }
        }
    }

    private class DictionaryDiffCallback : DiffUtil.ItemCallback<DictionaryEntry>() {
        override fun areItemsTheSame(oldItem: DictionaryEntry, newItem: DictionaryEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DictionaryEntry, newItem: DictionaryEntry): Boolean {
            return oldItem == newItem
        }
    }
} 