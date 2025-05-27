package com.project.peoplelist.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.core.User
import com.project.network.extensions.ApiResult
import com.project.network.extensions.handleApiResult
import com.project.peoplelist.domain.usecase.GetUserByIdUseCase
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val userId: Int,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<ApiResult.Error<User>?>()
    val error: LiveData<ApiResult.Error<User>?> = _error

    private val _cacheMessage = MutableLiveData<String?>()
    val cacheMessage: LiveData<String?> = _cacheMessage

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _cacheMessage.value = null

            val result = getUserByIdUseCase(userId)
            handleApiResult(
                result = result,
                onSuccess = { _user.value = it },
                onError = { message -> _error.value = ApiResult.Error(message) },
                onCache = { data, message ->
                    _user.value = data
                    _cacheMessage.value = message
                }
            )

            _isLoading.value = false
        }
    }

    fun retry() {
        loadUser()
    }

    fun clearCacheMessage() {
        _cacheMessage.value = null
    }
}