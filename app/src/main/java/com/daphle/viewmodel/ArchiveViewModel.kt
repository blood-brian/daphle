package com.daphle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daphle.data.PuzzleInfo
import com.daphle.data.PuzzleRepository
import com.daphle.data.PuzzleResult
import com.daphle.data.GameProgressStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val repository: PuzzleRepository,
    val wordLength: Int,
) : ViewModel() {

    val puzzles = repository.puzzlesFlow(wordLength)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPuzzleCompleted(index: Int, won: Boolean) {
        viewModelScope.launch {
            val result = if (won) PuzzleResult.WIN else PuzzleResult.LOSS
            repository.saveCompletion(wordLength, index, result)

            // Check if we should unlock the next batch
            val allPuzzles = puzzles.value
            val unlockedBatch = repository.unlockedBatchFlow(wordLength)
            val batchEnd = minOf(
                (allPuzzles.size / GameProgressStore.BATCH_SIZE + 1) * GameProgressStore.BATCH_SIZE,
                allPuzzles.size
            )
            // Unlock next batch if all unlocked puzzles are completed
            val unlocked = allPuzzles.filter { it.isUnlocked }
            val allCompleted = unlocked.all { it.result != PuzzleResult.NONE && it.result != PuzzleResult.IN_PROGRESS }
            if (allCompleted && unlocked.size < allPuzzles.size) {
                repository.unlockNextBatch(wordLength)
            }
        }
    }

    class Factory(
        private val repository: PuzzleRepository,
        private val wordLength: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ArchiveViewModel(repository, wordLength) as T
    }
}
