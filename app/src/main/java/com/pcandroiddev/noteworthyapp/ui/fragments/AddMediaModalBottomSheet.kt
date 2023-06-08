package com.pcandroiddev.noteworthyapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pcandroiddev.noteworthyapp.databinding.ModalBottomSheetAddMediaBinding
import com.pcandroiddev.noteworthyapp.util.Utils
import com.pcandroiddev.noteworthyapp.viewmodel.NoteViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddMediaModalBottomSheet : BottomSheetDialogFragment() {

    private var _binding: ModalBottomSheetAddMediaBinding? = null
    private val binding: ModalBottomSheetAddMediaBinding get() = _binding!!


    private val addImageViewModel by activityViewModels<NoteViewModel>()


    private lateinit var imageUri: Uri

    private val chooseImageContracts =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                val multipartBodyPartList: MutableList<MultipartBody.Part> = mutableListOf()
                for ((index, contentUri) in uris.withIndex()) {
                    multipartBodyPartList.add(index, prepareFilePart(contentUri))
                }

                addImageViewModel.uploadImage(multipartBodyPartList = multipartBodyPartList)
                Log.d(
                    "AddMediaModalBottomSheet",
                    "registerForActivityResult: $multipartBodyPartList"
                )
                Log.d(
                    "AddMediaModalBottomSheet",
                    "registerForActivityResult/SelectedImageUriList: $uris"
                )
                dismiss()
            }
        }

    private val takePhotoContract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            val multipartBodyPartList: MutableList<MultipartBody.Part> = mutableListOf()
            multipartBodyPartList.add(0, prepareFilePart(imageUri))
            addImageViewModel.uploadImage(multipartBodyPartList = multipartBodyPartList)

            Log.d("AddMediaModalBottomSheet", "registerForActivityResult: $multipartBodyPartList")
            Log.d(
                "AddMediaModalBottomSheet",
                "registerForActivityResult/SelectedImageUriList: $imageUri"
            )
            dismiss()
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ModalBottomSheetAddMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAddImage.setOnClickListener {
            chooseImageContracts.launch("image/*")
        }

        binding.tvTakePhoto.setOnClickListener {
            imageUri = createImageUri()!!
            takePhotoContract.launch(imageUri)
        }
    }


    private fun createImageUri(): Uri? {
        val image = File(requireContext().filesDir, "${Utils.getFileNameStamp()}.png")
        return FileProvider.getUriForFile(
            requireContext(),
            "com.pcandroiddev.noteworthyapp.fileProvider",
            image
        )
    }


    private fun prepareFilePart(uri: Uri): MultipartBody.Part {
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