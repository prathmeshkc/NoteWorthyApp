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
import com.pcandroiddev.noteworthyapp.models.note.ImgUrl
import javax.inject.Inject

class ImageAdapter @Inject constructor(
    private val glide: RequestManager,
    private val onImageClicked: (ImgUrl, Int) -> Unit,
    private val onImageDeleteClicked: (ImgUrl) -> Unit
) :
    ListAdapter<ImgUrl, ImageAdapter.ImageViewHolder>(ComparatorDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = getItem(position)
        uri?.let {
            holder.bind(imgUrl = it, position = position)
        }
    }

    inner class ImageViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: ImgUrl, position: Int) {

            Log.d("ImageAdapter", "bind: $imgUrl")
            glide.load(imgUrl.public_url).into(binding.image)


            binding.root.setOnClickListener {
                onImageClicked(imgUrl, position)
            }

            binding.btnDelete.setOnClickListener {
                onImageDeleteClicked(imgUrl)
            }
        }
    }


    class ComparatorDiffUtil : DiffUtil.ItemCallback<ImgUrl>() {
        override fun areItemsTheSame(oldItem: ImgUrl, newItem: ImgUrl): Boolean {
            return oldItem.toString() == newItem.toString()
        }

        override fun areContentsTheSame(oldItem: ImgUrl, newItem: ImgUrl): Boolean {
            return oldItem == newItem
        }

    }

}