package com.avicodes.halchalin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.avicodes.halchalin.domain.repository.PhoneAuthRepository
import com.avicodes.halchalin.domain.repository.UserRespository

class MainActivityViewModelFactory(
    private val phoneAuthRepository: PhoneAuthRepository,
    private val userRespository: UserRespository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(
            phoneAuthRepository = phoneAuthRepository,
            userRespository = userRespository
        ) as T
    }
}