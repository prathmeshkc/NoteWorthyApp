package com.pcandroiddev.noteworthyapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.databinding.ModalBottomSheetAddMediaBinding
import com.pcandroiddev.noteworthyapp.viewmodel.NoteViewModel

class AddMediaModalBottomSheet : BottomSheetDialogFragment(R.layout.modal_bottom_sheet_add_media) {

    private var _binding: ModalBottomSheetAddMediaBinding? = null

    private val binding: ModalBottomSheetAddMediaBinding get() = _binding!!

    private val noteViewModel by viewModels<NoteViewModel>()


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

        }
    }


}