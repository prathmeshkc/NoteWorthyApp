package com.pcandroiddev.noteworthyapp.models.note

data class NoteRequest(
    val images: List<ImgUrl>,
    val title: String,
    val description: String,
    val priority: String
)