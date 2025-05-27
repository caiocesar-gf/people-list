package com.project.peoplelist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.core.User
import com.project.network.extensions.handleApiResult
import com.project.peoplelist.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

data class UserListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cacheMessage: String? = null
)

class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    fun setError(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun setCacheMessage(message: String) {
        _state.value = _state.value.copy(cacheMessage = message)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val users: Flow<PagingData<User>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            getUsersUseCase(
                searchQuery = query.trim(),
                onCacheUsed = { message ->
                    setCacheMessage(message)
                }
            )
        }
        .cachedIn(viewModelScope)

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        // Implementar lógica de refresh se necessário
    }

    fun retry() {
        // Implementar lógica de retry se necessário
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearCacheMessage() {
        _state.value = _state.value.copy(cacheMessage = null)
    }
}