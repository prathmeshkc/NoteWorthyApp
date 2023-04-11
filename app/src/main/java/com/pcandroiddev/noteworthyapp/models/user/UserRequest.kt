package com.pcandroiddev.noteworthyapp.models.user

data class UserRequest(
    val email: String,
    val password: String,
    val username: String
)