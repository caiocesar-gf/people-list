package com.project.peoplelist.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.core.User
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

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val user = getUserByIdUseCase(userId)
                _user.value = user
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar usuário"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        loadUser()
    }
}