package com.daphle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daphle.data.PuzzleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: PuzzleRepository) : ViewModel() {

    val hardMode: StateFlow<Boolean> = repository.hardModeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleHardMode() {
        viewModelScope.launch {
            val current = hardMode.value
            repository.setHardMode(!current)
        }
    }

    class Factory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
