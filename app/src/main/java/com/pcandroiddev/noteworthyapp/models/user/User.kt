package com.pcandroiddev.noteworthyapp.models.user


data class User(
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val authorities: List<Authority>,
    val credentialsNonExpired: Boolean,
    val email: String,
    val enabled: Boolean,
    val id: Int,
    val password: String,
    val role: String,
    val username: String
)