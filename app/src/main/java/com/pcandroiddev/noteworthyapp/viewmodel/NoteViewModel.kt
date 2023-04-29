package com.pcandroiddev.noteworthyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.repository.NoteRepository
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {

    val notesLiveData: LiveData<NetworkResults<List<NoteResponse>>> get() = noteRepository.notesLiveData
    val statusLiveData: LiveData<NetworkResults<String>> get() = noteRepository.statusLiveData

    val shareByEmailLiveData: LiveData<NetworkResults<String>> get() = noteRepository.shareByEmailLiveData

    fun getNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getNotes()
        }
    }

    fun sortNotesByPriority(sortBy: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.sortNotesByPriority(sortBy = sortBy)
        }
    }


    fun searchNotes(searchText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.searchNotes(searchText = searchText)
        }
    }


    fun createNotes(
        noteRequest: NoteRequest
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.createNote(noteRequest = noteRequest)
        }
    }

    fun updateNotes(
        noteId: String,
        noteRequest: NoteRequest
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(
                noteId = noteId,
                noteRequest = noteRequest
            )
        }
    }

    fun deleteNotes(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(noteId = noteId)
        }
    }

    fun shareNoteByEmail(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.shareNoteByEmail(noteId = noteId)
        }
    }


}