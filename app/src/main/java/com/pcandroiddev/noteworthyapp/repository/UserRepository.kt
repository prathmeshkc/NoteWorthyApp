package com.pcandroiddev.noteworthyapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pcandroiddev.noteworthyapp.api.UserService
import com.pcandroiddev.noteworthyapp.models.user.UserRequest
import com.pcandroiddev.noteworthyapp.models.user.UserResponse
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(private val userService: UserService) {

    private val _userResponseLiveData = MutableLiveData<NetworkResults<UserResponse>>()
    val userResponseLiveData: LiveData<NetworkResults<UserResponse>> get() = _userResponseLiveData

    suspend fun registerUser(userRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResults.Loading())
        val response = userService.register(userRequest = userRequest)
        handleResponse(response)
    }

    suspend fun loginUser(userRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResults.Loading())
        val response = userService.login(userRequest = userRequest)
        handleResponse(response)
    }

    /**
     * TODO: Handle 403 Forbidden response for refresh token
     */
    private fun handleResponse(response: Response<UserResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _userResponseLiveData.postValue(NetworkResults.Success(data = response.body()!!))
        } else if (response.errorBody() != null) {
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _userResponseLiveData.postValue(NetworkResults.Error(message = errorObj.getString("message")))
        } else {
            _userResponseLiveData.postValue(NetworkResults.Error(message = "Something Went Wrong!"))
        }
    }

}