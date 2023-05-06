package com.pcandroiddev.noteworthyapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pcandroiddev.noteworthyapp.api.NoteService
import com.pcandroiddev.noteworthyapp.models.image.DeleteImageResponse
import com.pcandroiddev.noteworthyapp.models.note.ImgUrl
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteService: NoteService) {

    private val _notesLiveData = MutableLiveData<NetworkResults<List<NoteResponse>>>()
    val notesLiveData: LiveData<NetworkResults<List<NoteResponse>>> get() = _notesLiveData

    private val _statusLiveData = MutableLiveData<NetworkResults<String>>()
    val statusLiveData: LiveData<NetworkResults<String>> get() = _statusLiveData

    private val _shareByEmailLiveData = MutableLiveData<NetworkResults<String>>()
    val shareByEmailLiveData: LiveData<NetworkResults<String>> get() = _shareByEmailLiveData

    private val _uploadImageUrlLiveData = MutableLiveData<NetworkResults<List<ImgUrl>>>()
    val uploadImageUrlLiveData: LiveData<NetworkResults<List<ImgUrl>>> get() = _uploadImageUrlLiveData

    private val _deleteImageLiveData = MutableLiveData<NetworkResults<DeleteImageResponse>>()
    val deleteImageLiveData: LiveData<NetworkResults<DeleteImageResponse>> get() = _deleteImageLiveData


    suspend fun getNotes() {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.getNotes()
        Log.d("NoteRepository", "getNotes service called")
        handleNotesLiveData(response = response, calledFrom = "getNotes")
    }

    suspend fun sortNotesByPriority(sortBy: String) {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.sortNotesByPriority(sortBy = sortBy)
        Log.d("NoteRepository", "sortNotesByPriority service called")
        handleNotesLiveData(response = response, calledFrom = "sortNotesByPriority")


    }

    suspend fun searchNotes(searchText: String) {
        _notesLiveData.postValue(NetworkResults.Loading())
        val response = noteService.searchNotes(searchText = searchText)
        Log.d("NoteRepository", "searchNotes service called")
        handleNotesLiveData(response = response, calledFrom = "searchNotes")
    }

    suspend fun createNote(
        noteRequest: NoteRequest
    ) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response = noteService.createNote(
            noteRequest = noteRequest
        )
        handleStatusLiveData(response = response, message = "Note Created!")

    }

    suspend fun updateNote(
        noteId: String, noteRequest: NoteRequest
    ) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response = noteService.updateNote(
            noteId = noteId, noteRequest = noteRequest
        )
        handleStatusLiveData(response = response, message = "Note Updated!")
    }

    suspend fun deleteNote(noteId: String) {
        _statusLiveData.postValue(NetworkResults.Loading())
        val response = noteService.deleteNote(noteId = noteId)
        handleStatusLiveData(response = response, message = "Note Deleted!")
    }

    suspend fun shareNoteByEmail(noteId: String) {
        _shareByEmailLiveData.postValue(NetworkResults.Loading())
        val response = noteService.shareNoteByEmail(noteId = noteId)
        handleShareByEmailResponse(response = response)
    }

    suspend fun uploadImage(multipartBodyPartList: List<MultipartBody.Part>) {
        _uploadImageUrlLiveData.postValue(NetworkResults.Loading())
        val response = noteService.uploadImages(images = multipartBodyPartList)
        handleImgUrlLiveData(response = response)
    }

    suspend fun deleteImage(publicId: String) {
        _deleteImageLiveData.postValue(NetworkResults.Loading())
        val response = noteService.deleteImage(publicId = publicId)
        handleDeleteImageStatusLiveData(response = response)
    }


    /*
    Can do changes in this method only by passing making it accept the livedata object(basically making it generic)
    instead of making handleShareByEmailResponse() method
     */
    private fun handleStatusLiveData(response: Response<NoteResponse>, message: String) {
        if (response.isSuccessful && response.body() != null) {
            _statusLiveData.postValue(NetworkResults.Success(message))
        } else {
            _statusLiveData.postValue(NetworkResults.Error("Something Went Wrong!"))
        }
    }


    private fun handleShareByEmailResponse(response: Response<NoteResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _shareByEmailLiveData.postValue(NetworkResults.Success("Email Sent"))
        } else {
            _shareByEmailLiveData.postValue(NetworkResults.Error("Something Went Wrong!"))
        }
    }

    private fun handleNotesLiveData(response: Response<List<NoteResponse>>, calledFrom: String) {
        if (response.isSuccessful && response.body() != null) {
            _notesLiveData.postValue(NetworkResults.Success(data = response.body()!!))
            Log.d("NoteRepository", "$calledFrom: $response")
        } else if (response.errorBody() != null) {
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _notesLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _notesLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

    private fun handleImgUrlLiveData(response: Response<List<ImgUrl>>) {
        if (response.isSuccessful && response.body() != null) {
            _uploadImageUrlLiveData.postValue(NetworkResults.Success(data = response.body()!!))
            Log.d("NoteRepository", "uploadImage: $response")
        } else if (response.errorBody() != null) {
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _uploadImageUrlLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _uploadImageUrlLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

    private fun handleDeleteImageStatusLiveData(response: Response<DeleteImageResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _deleteImageLiveData.postValue(NetworkResults.Success(data = response.body()!!))
            Log.d("NoteRepository", "deleteImage: $response")
        } else if (response.errorBody() != null) {
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _deleteImageLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _deleteImageLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }

    }

}