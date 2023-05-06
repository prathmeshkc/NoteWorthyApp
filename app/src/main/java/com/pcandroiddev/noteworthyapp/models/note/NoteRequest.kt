package com.pcandroiddev.noteworthyapp.models.note

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class NoteRequest(
    val images: List<ImgUrl>,
    val title: String,
    val description: String,
    val priority: String
)