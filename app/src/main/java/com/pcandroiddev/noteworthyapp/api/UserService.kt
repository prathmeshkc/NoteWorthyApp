package com.pcandroiddev.noteworthyapp.api

import com.pcandroiddev.noteworthyapp.models.jwt.RefreshTokenRequest
import com.pcandroiddev.noteworthyapp.models.user.UserRequest
import com.pcandroiddev.noteworthyapp.models.user.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {

    @POST("/users/register")
    suspend fun register(@Body userRequest: UserRequest): Response<UserResponse>

    @POST("/users/login")
    suspend fun login(@Body userRequest: UserRequest): Response<UserResponse>


}
