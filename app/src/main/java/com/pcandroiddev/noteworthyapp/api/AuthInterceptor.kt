package com.pcandroiddev.noteworthyapp.api

import android.util.Log
import com.pcandroiddev.noteworthyapp.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val token = tokenManager.getToken()
        request.addHeader("Authorization","Bearer $token")
        Log.d("AuthInterceptor", "intercept: ${request.build()}")


        return chain.proceed(request.build())
    }
}