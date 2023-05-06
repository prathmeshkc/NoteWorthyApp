package com.pcandroiddev.noteworthyapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.pcandroiddev.noteworthyapp.MainActivity
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.adapters.NoteAdapter
import com.pcandroiddev.noteworthyapp.databinding.FragmentMainBinding
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.util.Constants.TAG
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import com.pcandroiddev.noteworthyapp.util.TokenManager
import com.pcandroiddev.noteworthyapp.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    private val noteViewModel by viewModels<NoteViewModel>()

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var adapter: NoteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        adapter =
            NoteAdapter(
                (activity as MainActivity).glide,
                ::onNoteClicked
            ) //Use "::" notation to convert a function into a lambda
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel.getNotes()
        binding.noteList.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.noteList.adapter = adapter

        binding.addNote.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_noteFragment)
        }

        binding.btnSortByPriority.setOnClickListener { view ->
            showMenu(view = view, menuRes = R.menu.menu_sort_by_priority)
        }

        binding.btnMenu.setOnClickListener { view ->
            showMenu(view = view, menuRes = R.menu.menu_settings)
        }

        binding.swipeToRefresh.setOnRefreshListener {
            noteViewModel.getNotes()
            binding.swipeToRefresh.isRefreshing = false
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText?.let { searchText ->
                    noteViewModel.searchNotes(searchText = searchText)
                }
                return true
            }

        })

        bindObservers()

    }

    private fun bindObservers() {
        noteViewModel.notesLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false

            when (it) {
                is NetworkResults.Success -> {
                    adapter.submitList(it.data)
                    if (it.data?.isEmpty() == true) {
                        binding.emptyListAnimation.visibility = View.VISIBLE
                        Log.d(TAG, "bindObservers EmptyResponse: ${it.data} ")
                    } else {
//                        binding.tvEmptyResponse.visibility = View.GONE
                        binding.emptyListAnimation.visibility = View.GONE
                        Log.d(TAG, "bindObservers Response: ${it.data} ")
                    }
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

    private fun onNoteClicked(noteResponse: NoteResponse) {
        val bundle = Bundle()
        bundle.putString("note", Gson().toJson(noteResponse))
        findNavController().navigate(R.id.action_mainFragment_to_noteFragment, bundle)

    }

    @SuppressLint("RestrictedApi")
    private fun showMenu(view: View, @MenuRes menuRes: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)



        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)

        } catch (e: Exception) {
            Log.e("Main", "Error showing menu icons.", e)
        } finally {
            popupMenu.show()
        }

        if (menuRes == R.menu.menu_settings) {
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                // Respond to menu item click.
                when (menuItem.itemId) {
                    R.id.logout -> {
                        tokenManager.deleteToken()
                        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        } else if (menuRes == R.menu.menu_sort_by_priority) {
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                // Respond to menu item click.
                when (menuItem.itemId) {
                    R.id.low -> {
                        noteViewModel.sortNotesByPriority(sortBy = "LOW")
                        true
                    }

                    R.id.high -> {
                        noteViewModel.sortNotesByPriority(sortBy = "HIGH")
                        true
                    }

                    R.id.recent -> {
                        noteViewModel.getNotes()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }



        popupMenu.setOnDismissListener {
            // Respond to popup being dismissed.
            Log.d("PopupMenu", "Dismissed")
        }


    }


    override fun onResume() {
        super.onResume()
        noteViewModel.getNotes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
