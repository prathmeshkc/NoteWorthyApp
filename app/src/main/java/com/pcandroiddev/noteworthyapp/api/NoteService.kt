package com.pcandroiddev.noteworthyapp.api

import com.pcandroiddev.noteworthyapp.models.MessageBody
import com.pcandroiddev.noteworthyapp.models.image.DeleteImageResponse
import com.pcandroiddev.noteworthyapp.models.note.ImgUrl
import com.pcandroiddev.noteworthyapp.models.note.NoteRequest
import com.pcandroiddev.noteworthyapp.models.note.NoteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface NoteService {

    @GET("/notes/")
    suspend fun getNotes(): Response<List<NoteResponse>>

    @GET("/notes/sortBy")
    suspend fun sortNotesByPriority(
        @Query("sortBy") sortBy: String
    ): Response<List<NoteResponse>>

    @GET("/notes/{searchText}")
    suspend fun searchNotes(
        @Path("searchText") searchText: String
    ): Response<List<NoteResponse>>

    @POST("/notes/")
    suspend fun createNote(
        @Body noteRequest: NoteRequest
    ): Response<NoteResponse>

    @PUT("/notes/{noteId}")
    suspend fun updateNote(
        @Path("noteId") noteId: String,
        @Body noteRequest: NoteRequest
    ): Response<NoteResponse>

    @DELETE("/notes/{noteId}")
    suspend fun deleteNote(
        @Path("noteId") noteId: String
    ): Response<NoteResponse>

    @POST("/notes/share/{noteId}")
    suspend fun shareNoteByEmail(
        @Path("noteId") noteId: String
    ): Response<NoteResponse>

    @Multipart
    @POST("/notes/image/upload-image")
    suspend fun uploadImages(
        @Part images: List<MultipartBody.Part> = listOf()
    ): Response<List<ImgUrl>>

    @DELETE("/notes/image/delete-image/{public_id}")
    suspend fun deleteImage(
        @Path("public_id") publicId: String
    ): Response<DeleteImageResponse>
}