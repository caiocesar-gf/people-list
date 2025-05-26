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
import kotlinx.coroutines.flow.flatMapLatest

data class UserListState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val users: Flow<PagingData<User>> = _searchQuery
        .flatMapLatest { query ->
            getUsersUseCase(query)
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        // Força reload do PagingSource
        val currentQuery = _searchQuery.value
        _searchQuery.value = ""
        _searchQuery.value = currentQuery
        _state.value = _state.value.copy(isLoading = false)
    }

    fun retry() {
        refresh()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}