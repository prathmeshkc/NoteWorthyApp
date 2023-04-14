package com.pcandroiddev.noteworthyapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pcandroiddev.noteworthyapp.api.NoteService
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteService: NoteService) {

    private val _notesLiveData = MutableLiveData<NetworkResults<List<NoteResponse>>>()
    val notesLiveData: LiveData<NetworkResults<List<NoteResponse>>> get() = _notesLiveData

    private val _statusLiveData = MutableLiveData<NetworkResults<String>>()
    val statusLiveData: LiveData<NetworkResults<String>> get() = _statusLiveData

    suspend fun getNotes() {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.getNotes()
        Log.d("NoteRepository", "getNotes service called")
        if (response.isSuccessful && response.body() != null) {
            _notesLiveData.postValue(NetworkResults.Success(data = response.body()!!))
        } else if (response.errorBody() != null) {
            Log.d("NoteRepository", "getNotes: $response")
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())

            _notesLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _notesLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

    suspend fun sortNotesByPriority(sortBy: String) {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.sortNotesByPriority(sortBy = sortBy)
        Log.d("NoteRepository", "sortNotesByPriority service called")
        if (response.isSuccessful && response.body() != null) {
            _notesLiveData.postValue(NetworkResults.Success(data = response.body()!!))
        } else if (response.errorBody() != null) {
            Log.d("NoteRepository", "sortNotesByPriority: $response")
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _notesLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _notesLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

    suspend fun searchNotes(searchText: String) {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.searchNotes(searchText = searchText)
        Log.d("NoteRepository", "searchNotes service called")
        if (response.isSuccessful && response.body() != null) {
            _notesLiveData.postValue(NetworkResults.Success(data = response.body()!!))
        } else if (response.errorBody() != null) {
            Log.d("NoteRepository", "searchNotes: $response")
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _notesLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _notesLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

    suspend fun createNote(
        noteRequest: NoteRequest
    ) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response =
            noteService.createNote(
                images = noteRequest.images,
                title = noteRequest.title,
                description = noteRequest.description,
                priority = noteRequest.priority
            )
        handleResponse(response = response, message = "Note Created!")

    }

    suspend fun updateNote(
        noteId: String,
        noteRequest: NoteRequest
    ) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response = noteService.updateNote(
            noteId = noteId,
            images = noteRequest.images,
            title = noteRequest.title,
            description = noteRequest.description,
            priority = noteRequest.priority
        )
        handleResponse(response = response, message = "Note Updated!")
    }

    suspend fun deleteNote(noteId: String) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response = noteService.deleteNote(noteId = noteId)
        handleResponse(response = response, message = "Note Deleted!")
    }

    private fun handleResponse(response: Response<NoteResponse>, message: String) {
        if (response.isSuccessful && response.body() != null) {
            _statusLiveData.postValue(NetworkResults.Success(message))
        } else {
            _statusLiveData.postValue(NetworkResults.Error("Something Went Wrong!"))
        }
    }

}