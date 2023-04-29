package com.pcandroiddev.noteworthyapp.ui.fragments

import android.content.Context
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
import com.pcandroiddev.noteworthyapp.util.OnImageDeletedListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding: FragmentImageBinding get() = _binding!!


    private lateinit var glide: RequestManager
    private var parentListener: OnImageDeletedListener? = null


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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentListener = parentFragment as? OnImageDeletedListener
    }

    private fun setInitialData() {
        binding.imageFragProgressBar.visibility = View.VISIBLE
        val imageUriString = arguments?.getString("image_uri")
        Log.d("ImageFragment", "setInitialData-imageUriString: $imageUriString")
        val imageUri = Uri.parse(imageUriString)
        Log.d("ImageFragment", "setInitialData-imageUri: $imageUri")

        if (imageUriString != null) {
            if (imageUriString.startsWith("https")) {
                glide.load(imageUriString).into(binding.ivFullImage)
                binding.imageFragProgressBar.visibility = View.GONE
            } else {
                binding.ivFullImage.setImageURI(imageUri)
                binding.imageFragProgressBar.visibility = View.GONE

            }
        }
    }


    private fun bindHandlers() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

//        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.deleteImage -> {
//                    Log.d("ImageFragment", "bindHandlers: Image Deleted")
//                    //TODO
//                    true
//                }
//
//                else -> {
//                    Log.d("ImageFragment", "bindHandlers: Something went wrong!")
//                    false
//                }
//            }
//        }
    }

}
