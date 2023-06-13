package com.pcandroiddev.noteworthyapp.api

import android.util.Log
import com.pcandroiddev.noteworthyapp.models.jwt.RefreshTokenRequest
import com.pcandroiddev.noteworthyapp.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val tokenService: TokenService
) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {

        val token = tokenManager.getToken()
        val request = chain.request()
        if (!token.isNullOrEmpty()) {
            val newRequest = request
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            val response = chain.proceed(newRequest)
            return if (response.code == 403) {
                tokenManager.deleteToken()
                response.close()
                refreshToken(chain, request)
            } else {
                response
            }
        } else {
            return refreshToken(chain, request)
        }
    }

    private fun refreshToken(chain: Interceptor.Chain, request: Request): Response {
        val response = tokenService
            .refreshJWT(refreshTokenRequest = RefreshTokenRequest(email = tokenManager.getUserEmail()!!))
            .execute()

        return if (response.isSuccessful) {
            val token = response.body()?.token.toString()
            tokenManager.saveToken(token = token)
            val newRequest = request
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}