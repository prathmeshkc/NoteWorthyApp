package com.pcandroiddev.noteworthyapp.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.pcandroiddev.noteworthyapp.databinding.ImageItemBinding
import javax.inject.Inject

class ImageAdapter @Inject constructor(private val glide: RequestManager) :
    ListAdapter<Uri, ImageAdapter.ImageViewHolder>(ComparatorDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = getItem(position)
        uri?.let {
            holder.bind(it)
        }
    }

    inner class ImageViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            val uriToString = uri.toString()
            Log.d("ImageAdapter", "bind: $uriToString")
            if (uriToString.startsWith("https")) {
                glide.load(uriToString).into(binding.image)
            } else {
                binding.image.setImageURI(uri)
            }
        }
    }


    class ComparatorDiffUtil : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem.toString() == newItem.toString()
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

    }

}