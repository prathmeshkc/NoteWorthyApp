package com.pcandroiddev.noteworthyapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.databinding.NoteItemBinding
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse

class NoteAdapter(private val onNoteClicked: (NoteResponse) -> Unit) :
    ListAdapter<NoteResponse, NoteAdapter.NoteViewHolder>(
        ComparatorDiffUtil()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        note?.let {
            holder.bind(it)
        }
    }


    inner class NoteViewHolder(private val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteResponse) {

            val priorityColor = when (note.priority) {
                "LOW" -> R.drawable.priority_color_low
                "MEDIUM" -> R.drawable.priority_color_medium
                "HIGH" -> R.drawable.priority_color_high
                else -> {
                    R.drawable.priority_color_low
                }
            }

            binding.title.text = note.title
            binding.desc.text = note.description
            binding.imvPriorityColor.setImageResource(priorityColor)
            binding.root.setOnClickListener {
                onNoteClicked(note)
            }
        }
    }

    class ComparatorDiffUtil : DiffUtil.ItemCallback<NoteResponse>() {
        override fun areItemsTheSame(oldItem: NoteResponse, newItem: NoteResponse): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: NoteResponse, newItem: NoteResponse): Boolean {
            return oldItem == newItem
        }

    }
}