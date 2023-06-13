package com.pcandroiddev.noteworthyapp.api

import com.pcandroiddev.noteworthyapp.models.jwt.RefreshTokenRequest
import com.pcandroiddev.noteworthyapp.models.user.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("/token/refreshJWT")
    fun refreshJWT(@Body refreshTokenRequest: RefreshTokenRequest): Call<UserResponse>
}