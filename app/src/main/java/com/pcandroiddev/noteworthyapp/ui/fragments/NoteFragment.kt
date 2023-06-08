package com.pcandroiddev.noteworthyapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import java.lang.StringBuilder

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding: FragmentNoteBinding get() = _binding!!

    private var note: NoteResponse? = null

    //try changing this to by viewmodels<>() or detach VM in onDestroyView()
    private val noteSharedViewModel by viewModels<NoteViewModel>()

    private lateinit var imageAdapter: ImageAdapter

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

        setupDropDownArrayAdapter()
        setInitialData()
        setupRecyclerViewAdapter()
        bindHandlers()
        bindObservers()
    }


    private fun setupRecyclerViewAdapter() {
        binding.rvImages.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rvImages.adapter = imageAdapter
        binding.rvImages.setHasFixedSize(false)
        binding.rvImages.requestLayout()
    }

    private fun bindHandlers() {

        binding.topAppBar.setOnClickListener {
            findNavController().popBackStack()
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

            if ((TextUtils.isEmpty(title) || title.isBlank()) && (TextUtils.isEmpty(description) || description.isBlank()) && imgUrlList.isEmpty()) {
                Snackbar.make(binding.root, "Empty Note Discarded", Snackbar.LENGTH_LONG).show()
                findNavController().popBackStack()

            } else {
                if (note != null) {

                    noteSharedViewModel.updateNotes(
                        noteId = (note!!.noteId).toString(), noteRequest = NoteRequest(
                            images = imgUrlList,
                            title = title,
                            description = description,
                            priority = priority
                        )
                    )
                } else {
                    noteSharedViewModel.createNotes(
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

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.add_media_box -> {
                    findNavController().navigate(R.id.action_noteFragment_to_addMediaModalBottomSheet)
                    true
                }


                R.id.share_note -> {
                    note?.let {
//                        noteSharedViewModel.shareNoteByEmail(noteId = (it.noteId).toString())
                        shareNote(it)
                    }
                    true
                }

                R.id.delete_note -> {
                    note?.let {
                        noteSharedViewModel.deleteNotes(noteId = (it.noteId).toString())
                    }
                    true
                }

                else -> {
                    false
                }
            }

        }

    }

    private fun bindObservers() {
        noteSharedViewModel.statusLiveData.observe(viewLifecycleOwner) {
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

        noteSharedViewModel.shareByEmailLiveData.observe(viewLifecycleOwner) {
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

        noteSharedViewModel.uploadImageUrlLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false

            when (it) {
                is NetworkResults.Success -> {
                    val currentList = imageAdapter.currentList.toMutableList()
                    currentList.addAll(it.data!!)
                    val updatedList = currentList.toList()
                    imageAdapter.submitList(updatedList)
                    Log.d(
                        "NoteFragment", "NoteFragment bindObservers updatedList: $updatedList"
                    )
                    if (it.data.isEmpty()) {
                        binding.rvImages.visibility = View.GONE
                        Log.d(
                            "NoteFragment",
                            "NoteFragment bindObservers imageUrlLiveData EmptyResponse: ${it.data}"
                        )
                    } else {
                        binding.rvImages.visibility = View.VISIBLE
                        Log.d(
                            "NoteFragment",
                            "NoteFragment bindObservers imageUrlLiveData: ${imageAdapter.currentList}"
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

        noteSharedViewModel.deleteImageLiveData.observe(viewLifecycleOwner) { deleteImageResponse ->
            binding.progressBar.isVisible = false

            when (deleteImageResponse) {
                is NetworkResults.Success -> {

                    val currentList = imageAdapter.currentList
                    Log.d("NoteFragment", "deleteImageLiveData/currentList: $currentList")

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
                        requireView(), deleteImageResponse.message.toString(), Snackbar.LENGTH_LONG
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
                    //TODO: Below solution is just a work around. Still solid improvements are required
                    if (imageAdapter.currentList.isNotEmpty()) {
                        Log.d("NoteFragment", "setInitialData/isNotEmpty(): true")
                        val currentList = imageAdapter.currentList.toMutableList()
                        currentList.addAll(noteResponse.img_urls)
                        val updatedList = currentList.toList()
                        imageAdapter.submitList(updatedList)
                    } else {
                        Log.d("NoteFragment", "setInitialData/isNotEmpty(): false")
                        imageAdapter.submitList(noteResponse.img_urls)
                    }

                } else {
                    binding.rvImages.visibility = View.GONE
                }
            }
        } else {
            Log.d("NoteFragment", "setInitialData called/ jsonNote == null")
            binding.topAppBar.title = "Add Note"
            val menu = binding.bottomAppBar.menu
            val btnDelete = menu.findItem(R.id.delete_note)
            val btnShare = menu.findItem(R.id.share_note)
            btnDelete.isVisible = false
            btnShare.isVisible = false
        }
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
        noteSharedViewModel.deleteImage(publicId = imgUrl.public_id)
    }

    private fun shareNote(noteResponse: NoteResponse) {
        val sharedContent = StringBuilder()
        sharedContent.append("Title: ").append(noteResponse.title).append("\n\n")
        sharedContent.append("Description: ").append(noteResponse.description).append("\n\n\n")
        sharedContent.append("Priority: ").append(noteResponse.priority).append("\n\n")
        sharedContent.append("Attachments:- \n\n")

        for ((index, imgUrl) in noteResponse.img_urls.withIndex()) {
            sharedContent.append("${index + 1})\t").append(imgUrl.public_url).append("\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharedContent.toString())
        startActivity(Intent.createChooser(shareIntent, "Share Note"))

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
        noteSharedViewModel.uploadImageUrlLiveData.removeObservers(viewLifecycleOwner)
        noteSharedViewModel.statusLiveData.removeObservers(viewLifecycleOwner)
        noteSharedViewModel.shareByEmailLiveData.removeObservers(viewLifecycleOwner)
        noteSharedViewModel.deleteImageLiveData.removeObservers(viewLifecycleOwner)
        imageAdapter.submitList(emptyList())
        _binding = null
        Log.d("NoteFragment", "onDestroyView() called")
        super.onDestroyView()

    }


}