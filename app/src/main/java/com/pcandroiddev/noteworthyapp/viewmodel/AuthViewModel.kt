package com.pcandroiddev.noteworthyapp.viewmodel

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcandroiddev.noteworthyapp.models.user.UserRequest
import com.pcandroiddev.noteworthyapp.models.user.UserResponse
import com.pcandroiddev.noteworthyapp.repository.UserRepository
import com.pcandroiddev.noteworthyapp.util.Constants.TAG
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    val userResponseLiveData: LiveData<NetworkResults<UserResponse>> get() = userRepository.userResponseLiveData


    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.registerUser(userRequest = userRequest)
        }
    }

    fun loginUser(userRequest: UserRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.loginUser(userRequest = userRequest)
        }
    }

    fun validateCredentials(
        username: String,
        emailAddress: String,
        password: String,
        isLogin: Boolean
    ): Pair<Boolean, String> {

        var result = Pair(true, "")

        /* Check if any of the field is empty */
        if (TextUtils.isEmpty(emailAddress) || (!isLogin && TextUtils.isEmpty(username)) || TextUtils.isEmpty(password)) {
            result = Pair(false, "Please provide the credentials!")
        }
        /* Check email format/pattern */
        else if ((!TextUtils.isEmpty(emailAddress)) && (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches())) {
            result = Pair(false, "Please provide valid email address!")
        }
        /*Check password length*/
        else if (!TextUtils.isEmpty(password) && password.length <= 5) {
            result = Pair(false, "Password should be more than 5 characters!")
        }
        Log.d(TAG, "validateCredentials: $result")
        return result
    }
}
