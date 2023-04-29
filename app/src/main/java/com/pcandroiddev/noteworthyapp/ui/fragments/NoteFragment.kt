package com.pcandroiddev.noteworthyapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pcandroiddev.noteworthyapp.MainActivity
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.adapters.ImageAdapter
import com.pcandroiddev.noteworthyapp.databinding.FragmentNoteBinding
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import com.pcandroiddev.noteworthyapp.util.OnImageDeletedListener
import com.pcandroiddev.noteworthyapp.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class NoteFragment : Fragment(), OnImageDeletedListener {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    private var note: NoteResponse? = null
    private val noteViewModel by viewModels<NoteViewModel>()
    private var uriList: MutableList<Uri> = mutableListOf()

    private lateinit var imageAdapter: ImageAdapter


    private val contracts =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
//            uriList = it.toMutableList()
            uriList.addAll(it.toMutableList())
            Log.d("NoteFragment", "registerForActivityResult: $uriList")
            imageAdapter.submitList(uriList.toList())
            Log.d("NoteFragment", "SelectedImageUriList: $uriList")
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        imageAdapter = ImageAdapter((activity as MainActivity).glide, ::onImageClicked)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropDownArrayAdapter()
        setInitialData()
        setupRecyclerViewAdapter()
        bindHandlers()
        bindObservers()
    }

    private fun setupRecyclerViewAdapter() {
        binding.rvImages.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
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
            val listOfPart: MutableList<MultipartBody.Part> = mutableListOf()
            for (index in uriList.indices) {
                listOfPart.add(index, prepareFilePart(uriList[index], index))
            }

            val titleRequestBody = title.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val descRequestBody =
                description.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val priorityRequestBody =
                priority.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            Log.d("NoteFragment", "title: $title")
            Log.d("NoteFragment", "description: $description")
            Log.d("NoteFragment", "priority: $priority")


            if (note != null) {
                noteViewModel.updateNotes(
                    noteId = (note!!.noteId).toString(),
                    noteRequest = NoteRequest(
                        images = listOfPart.toList(),
                        title = titleRequestBody,
                        description = descRequestBody,
                        priority = priorityRequestBody
                    )
                )
            } else {
                noteViewModel.createNotes(
                    noteRequest = NoteRequest(
                        images = listOfPart.toList(),
                        title = titleRequestBody,
                        description = descRequestBody,
                        priority = priorityRequestBody
                    )
                )

            }
            Log.d("NoteFragment", "MultipartList: $listOfPart")
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
                        .setAction("OK") {
                            // Do nothing, just dismiss the Snackbar
                        }
                        .show()
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
    }

    private fun setInitialData() {
        val jsonNote = arguments?.getString("note")
        if (jsonNote != null) {
            note = Gson().fromJson(jsonNote, NoteResponse::class.java)
            note?.let { noteResponse ->
                val listOfImageUrls: MutableList<Uri> = mutableListOf()
                binding.txtTitle.setText(noteResponse.title)
                binding.txtDescription.setText(noteResponse.description)
                binding.actvPriority.setText(noteResponse.priority)

                for (index in noteResponse.img_urls.indices) {
                    listOfImageUrls.add(index, noteResponse.img_urls[index].public_url.toUri())
                }
                imageAdapter.submitList(listOfImageUrls)
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
        val file = File(filesDir, "image_${index}.png")
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

    private fun onImageClicked(uriToString: String) {
        Log.d("NoteFragment", "onImageClicked: $uriToString")
        val bundle = Bundle()
        bundle.putString("image_uri", uriToString)
        findNavController().navigate(R.id.action_noteFragment_to_imageFragment, bundle)
    }

    override fun onImageDeleted(imageUri: Uri) {
        val images = imageAdapter.currentList.toMutableList()
        images.remove(imageUri)
        imageAdapter.submitList(images.toList())
    }

    /**
    Also call setupDropDownArrayAdapter() in onResume because when you navigate to other fragment
    and navigate back to this fragment, the onResume() will be called
    and the arrayAdapter will be setup up again.
     */
    override fun onResume() {
        super.onResume()
        setupDropDownArrayAdapter()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}