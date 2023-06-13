package com.pcandroiddev.noteworthyapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.pcandroiddev.noteworthyapp.util.Constants.PREFS_TOKEN_FILE
import com.pcandroiddev.noteworthyapp.util.Constants.USER_EMAIL
import com.pcandroiddev.noteworthyapp.util.Constants.USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenManager @Inject constructor(@ApplicationContext context: Context) {
    private var prefs = context.getSharedPreferences(PREFS_TOKEN_FILE, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    fun saveToken(token: String) {
        editor.putString(USER_TOKEN, token)
        Log.d("TokenManager", "saveToken: $token")
        editor.apply()
    }

    fun saveUserEmail(userEmail: String) {
        editor.putString(USER_EMAIL, userEmail)
        Log.d("TokenManager", "saveUserEmail: $userEmail")
        editor.apply()
    }

    fun getToken(): String? {
        val token = prefs.getString(USER_TOKEN, null)
        Log.d("TokenManager", "getToken: $token")

        return token
    }

    fun getUserEmail(): String? {
        val userEmail = prefs.getString(USER_EMAIL, null)
        Log.d("TokenManager", "getUserEmail: $userEmail")
        return userEmail
    }

    fun deleteToken() {
        editor.remove(USER_TOKEN)
        editor.apply()
    }

    fun deleteUserEmail() {
        editor.remove(USER_EMAIL)
        editor.apply()
    }


}

