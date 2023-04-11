package com.pcandroiddev.noteworthyapp.models.note

import com.pcandroiddev.noteworthyapp.models.note.ImgUrl

data class NoteResponse(
    val description: String,
    val img_urls: List<ImgUrl>,
    val noteId: Int,
    val title: String,
    val userId: Int
)