package com.project.peoplelist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.core.User
import com.project.peoplelist.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserListState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    val users: Flow<PagingData<User>> = getUsersUseCase()
        .cachedIn(viewModelScope)

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        _state.value = _state.value.copy(isLoading = false)
    }

    fun retry() {
        refresh()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}