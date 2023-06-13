package com.pcandroiddev.noteworthyapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.pcandroiddev.noteworthyapp.MainActivity
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.databinding.FragmentImageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding: FragmentImageBinding get() = _binding!!


    private lateinit var glide: RequestManager

    private var imageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentImageBinding.inflate(inflater, container, false)
        glide = (activity as MainActivity).glide
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialData()
        bindHandlers()
    }

    private fun setInitialData() {
        binding.imageFragProgressBar.visibility = View.VISIBLE
        imageUrl = arguments?.getString("image_url")
        Log.d("ImageFragment", "setInitialData-imageUriString: $imageUrl")
        Log.d("ImageFragment", "setInitialData-imageUri: $imageUrl")

        if (imageUrl != null) {
            glide.load(imageUrl).into(binding.ivFullImage)
            binding.imageFragProgressBar.visibility = View.GONE
        }
    }


    private fun bindHandlers() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.download_image -> {
                    imageUrl?.let { url ->
                        (activity as MainActivity).imageDownloader.downloadFile(url)
                    }
                    true
                }

                else -> {
                    Log.d("ImageFragment", "bindHandlers: Image Download Failed")
                    false
                }
            }

        }


    }

}
