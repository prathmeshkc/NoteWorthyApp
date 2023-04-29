package com.pcandroiddev.noteworthyapp.models.note

data class NoteResponse(
    val description: String,
    val img_urls: List<ImgUrl>,
    val noteId: Int,
    val priority: String,
    val title: String,
    val userId: Int
)
