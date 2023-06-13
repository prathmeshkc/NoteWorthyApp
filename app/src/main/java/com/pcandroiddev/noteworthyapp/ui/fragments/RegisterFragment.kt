package com.pcandroiddev.noteworthyapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pcandroiddev.noteworthyapp.R
import com.pcandroiddev.noteworthyapp.databinding.FragmentRegisterBinding
import com.pcandroiddev.noteworthyapp.models.user.UserRequest
import com.pcandroiddev.noteworthyapp.util.NetworkResults
import com.pcandroiddev.noteworthyapp.util.TokenManager
import com.pcandroiddev.noteworthyapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        if (tokenManager.getToken() != null) {
            findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val validationResult = validateUserInputs()
            if (validationResult.first) {
                authViewModel.registerUser(getUserRequest())
            } else {
                binding.txtError.text = validationResult.second
            }
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
//            throw RuntimeException("Firebase Crashlytics Test Crash")
        }

        bindObserver()
    }

    private fun getUserRequest(): UserRequest {
        val emailAddress = binding.txtEmail.text.toString()
        val password = binding.txtPassword.text.toString()
        val username = binding.txtUsername.text.toString()
        return UserRequest(email = emailAddress, password = password, username = username)
    }

    private fun validateUserInputs(): Pair<Boolean, String> {
        val userRequest = getUserRequest()
        return authViewModel.validateCredentials(
            username = userRequest.username,
            emailAddress = userRequest.email,
            password = userRequest.password,
            isLogin = false
        )
    }

    private fun bindObserver() {
        authViewModel.userResponseLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            when (it) {
                is NetworkResults.Success -> {
                    tokenManager.saveToken(it.data!!.token)
                    tokenManager.saveUserEmail(it.data.user.email)
                    findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
                }

                is NetworkResults.Error -> {
                    binding.txtError.text = it.message
                }

                is NetworkResults.Loading -> {
                    binding.progressBar.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}