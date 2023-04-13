package com.pcandroiddev.noteworthyapp.models.note

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class NoteRequest(
    val images: List<MultipartBody.Part>,
    val title: RequestBody,
    val description: RequestBody,
    val priority: RequestBody
)