package com.pcandroiddev.noteworthyapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pcandroiddev.noteworthyapp.MainActivity
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.adapters.ImageAdapter
import com.pcandroiddev.noteworthyapp.databinding.FragmentNoteBinding
import com.pcandroiddev.noteworthyapp.models.note.ImgUrl
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.util.Constants
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import com.pcandroiddev.noteworthyapp.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    private var note: NoteResponse? = null
    private val noteViewModel by viewModels<NoteViewModel>()

    private lateinit var imageAdapter: ImageAdapter

//    private var recyclerViewState: Parcelable? = null


    private val contracts =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            /*
            TODO:
            1. Call a service to upload a list of images. uploadImages(List<Multipart> it): List<ImgUrl>.
            This should return a response in the form of ImgUrl(url, public_id).
            2. Add these ImgUrl list to the list in recycler view which will display the images uploaded by extracting
            the HTTP URLs.
            Keep calling uploadImages(it) with the newly chosen list<Uri> (i.e. it) in case of adding more images.
            And add those ImgUrl to the list in recycler view which will display the images uploaded.
            */

            if (it.isNotEmpty()) {
                val multipartBodyPartList: MutableList<MultipartBody.Part> = mutableListOf()
                for ((index, contentUri) in it.withIndex()) {
                    multipartBodyPartList.add(index, prepareFilePart(contentUri, index))
                }

                noteViewModel.uploadImage(multipartBodyPartList = multipartBodyPartList)
                Log.d("NoteFragment", "registerForActivityResult: $multipartBodyPartList")
                Log.d("NoteFragment", "registerForActivityResult/SelectedImageUriList: $it")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        imageAdapter =
            ImageAdapter((activity as MainActivity).glide, ::onImageClicked, ::onImageDeleteClicked)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (savedInstanceState != null) {
//            recyclerViewState = savedInstanceState.getParcelable("recycler_state")
//        }

        setupDropDownArrayAdapter()
        setInitialData()
        setupRecyclerViewAdapter()
        bindHandlers()
        bindObservers()
    }

    /* override fun onViewStateRestored(savedInstanceState: Bundle?) {
         super.onViewStateRestored(savedInstanceState)
         if (savedInstanceState != null) {
             recyclerViewState = savedInstanceState.getParcelable("recycler_state")
         }
     }*/

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            "recycler_state",
            binding.rvImages.layoutManager?.onSaveInstanceState()
        )
    }*/

    private fun setupRecyclerViewAdapter() {
        binding.rvImages.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rvImages.adapter = imageAdapter
        binding.rvImages.setHasFixedSize(false)
        binding.rvImages.requestLayout()
    }

    private fun bindHandlers() {
        binding.btnDelete.setOnClickListener {
            note?.let {
                noteViewModel.deleteNotes(noteId = (it.noteId).toString())
            }
        }

        binding.btnAddImage.setOnClickListener {
            contracts.launch("image/*")
        }

        binding.btnShare.setOnClickListener {
            note?.let {
                noteViewModel.shareNoteByEmail(noteId = (it.noteId).toString())
            }
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.txtTitle.text.toString()
            val description = binding.txtDescription.text.toString()
            val priority = binding.actvPriority.text.toString()
            val imgUrlList = imageAdapter.currentList.toList()

            Log.d("NoteFragment", "title: $title")
            Log.d("NoteFragment", "description: $description")
            Log.d("NoteFragment", "priority: $priority")
            Log.d("NoteFragment", "imgUrlList: $imgUrlList")

            if (note != null) {

                noteViewModel.updateNotes(
                    noteId = (note!!.noteId).toString(), noteRequest = NoteRequest(
                        images = imgUrlList,
                        title = title,
                        description = description,
                        priority = priority
                    )
                )
            } else {
                noteViewModel.createNotes(
                    noteRequest = NoteRequest(
                        images = imgUrlList,
                        title = title,
                        description = description,
                        priority = priority
                    )
                )
            }
            Log.d("NoteFragment", "ImageUrlList: $imgUrlList")
        }
    }

    private fun bindObservers() {
        noteViewModel.statusLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            when (it) {
                is NetworkResults.Success -> {
                    findNavController().popBackStack()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                is NetworkResults.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }

        noteViewModel.shareByEmailLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            when (it) {
                is NetworkResults.Success -> {
                    Snackbar.make(requireView(), "Email Sent!", Snackbar.LENGTH_LONG)
                        .setAction("OK") { }.show()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                is NetworkResults.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }

        noteViewModel.uploadImageUrlLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false

            when (it) {
                is NetworkResults.Success -> {
                    val currentList = imageAdapter.currentList.toMutableList()
                    currentList.addAll(it.data!!)
                    val updatedList = currentList.toList()
                    imageAdapter.submitList(updatedList)
                    Log.d(
                        Constants.TAG,
                        "NoteFragment bindObservers updatedList: $updatedList"
                    )
                    if (it.data.isEmpty()) {
                        binding.rvImages.visibility = View.GONE
                        Log.d(
                            Constants.TAG,
                            "NoteFragment bindObservers imageUrlLiveData EmptyResponse: ${it.data}"
                        )
                    } else {
                        binding.rvImages.visibility = View.VISIBLE
                        Log.d(
                            Constants.TAG,
                            "NoteFragment bindObservers imageUrlLiveData: ${it.data}"
                        )
                    }
                }

                is NetworkResults.Error -> {
                    Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                }

                is NetworkResults.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }

        noteViewModel.deleteImageLiveData.observe(viewLifecycleOwner) { deleteImageResponse ->
            binding.progressBar.isVisible = false

            when (deleteImageResponse) {
                is NetworkResults.Success -> {

                    val currentList = imageAdapter.currentList
                    val position =
                        currentList.indexOfFirst { it.public_id == deleteImageResponse.data?.public_id }
                    if (position != -1) {
                        Log.d("NoteFragment", "deleteImageLiveData/Position: $position")
                        val updatedList = currentList.toMutableList()
                        updatedList.removeAt(position)
                        imageAdapter.submitList(updatedList.toList())
                        Log.d("NoteFragment", "deleteImageLiveData/updatedList: $updatedList")

                    }
                }

                is NetworkResults.Error -> {
                    Snackbar.make(
                        requireView(),
                        deleteImageResponse.message.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                is NetworkResults.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }

    }

    private fun setInitialData() {
        Log.d("NoteFragment", "setInitialData called")
        val jsonNote = arguments?.getString("note")
        if (jsonNote != null) {
            note = Gson().fromJson(jsonNote, NoteResponse::class.java)
            note?.let { noteResponse ->
                binding.txtTitle.setText(noteResponse.title)
                binding.txtDescription.setText(noteResponse.description)
                binding.actvPriority.setText(noteResponse.priority)

                if (noteResponse.img_urls.isNotEmpty()) {
                    binding.rvImages.visibility = View.VISIBLE
                    imageAdapter.submitList(noteResponse.img_urls)
                } else {
                    binding.rvImages.visibility = View.GONE
                }
            }
        } else {
            binding.addEditText.text = "Add Note"
            binding.btnDelete.visibility = View.GONE
            binding.btnShare.visibility = View.GONE
        }
    }

    private fun prepareFilePart(uri: Uri, index: Int): MultipartBody.Part {
        Log.d("NoteFragment", "prepareFilePart: ${uri.path}")
        val filesDir = activity?.applicationContext?.filesDir
        val file = File(filesDir, "image_${getFileName(requireContext(), uri)}.png")
        file.createNewFile()
        val inputStream = activity?.applicationContext?.contentResolver?.openInputStream(uri)
        Log.d("NoteFragment", "imageToMultiPart: $inputStream")
        val outputStream = FileOutputStream(file)
        inputStream!!.copyTo(outputStream)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        Log.d("NoteFragment", "RequestBody: $requestBody")
        val part = MultipartBody.Part.createFormData("img_urls", file.name, requestBody)
        Log.d("NoteFragment", "MultipartBody.Part: $part")

        return part

    }

    private fun setupDropDownArrayAdapter() {
        val priorities = resources.getStringArray(R.array.priorities)
        val dropDownArrayAdapter =
            ArrayAdapter(requireActivity(), R.layout.dropdown_item, priorities)
        binding.actvPriority.setAdapter(dropDownArrayAdapter)
    }

    private fun onImageClicked(imgUrl: ImgUrl, position: Int) {
        Log.d("NoteFragment", "onImageClicked: $imgUrl - Position: $position")
        val bundle = Bundle()
        bundle.putString("image_url", imgUrl.public_url)
        findNavController().navigate(R.id.action_noteFragment_to_imageFragment, bundle)
    }

    /**
     * Should've passed just the public_id but passing the whole object just in case required later
     */
    private fun onImageDeleteClicked(imgUrl: ImgUrl) {
        noteViewModel.deleteImage(publicId = imgUrl.public_id)
    }

    /**
    Also call setupDropDownArrayAdapter() in onResume because when you navigate to other fragment
    and navigate back to this fragment, the onResume() will be called
    and the arrayAdapter will be setup up again.
     */
    override fun onResume() {
        super.onResume()
        setupDropDownArrayAdapter()
        setInitialData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
        }

        return uri.path!!.substring(uri.path!!.lastIndexOf('/') + 1)
    }
}