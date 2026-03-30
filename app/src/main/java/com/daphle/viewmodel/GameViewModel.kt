package com.daphle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daphle.data.PuzzleRepository
import com.daphle.data.PuzzleResult
import com.daphle.game.GameEngine
import com.daphle.game.GameState
import com.daphle.game.GameStatus
import com.daphle.game.GuessResult
import com.daphle.game.LetterResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val keyboardColors: Map<Char, LetterResult> = emptyMap(),
    val hardMode: Boolean = false,
)

class GameViewModel(
    private val repository: PuzzleRepository,
    val wordLength: Int,
    val puzzleIndex: Int,
) : ViewModel() {

    private val targetWord = repository.answerAt(wordLength, puzzleIndex)
    private lateinit var engine: GameEngine

    private val _uiState = MutableStateFlow<GameUiState?>(null)
    val uiState: StateFlow<GameUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hardMode = repository.hardModeFlow().first()
            engine = GameEngine(
                targetWord = targetWord,
                isValidWord = { repository.isValidGuess(it, wordLength) },
                hardMode = hardMode,
            )

            // Restore in-progress guesses if any
            val inProgress = repository.inProgressFlow(wordLength).first()
            if (inProgress != null && inProgress.puzzleIndex == puzzleIndex) {
                inProgress.guesses.forEach { engine.submitGuess(it) }
            }

            _uiState.value = GameUiState(
                gameState = engine.currentState,
                hardMode = hardMode,
                keyboardColors = computeKeyboardColors(engine.currentState),
            )
        }
    }

    fun onKey(char: Char) {
        val state = _uiState.value ?: return
        if (state.gameState.status != GameStatus.IN_PROGRESS) return
        if (state.currentInput.length >= wordLength) return
        _uiState.value = state.copy(
            currentInput = state.currentInput + char.uppercaseChar(),
            errorMessage = null,
        )
    }

    fun onBackspace() {
        val state = _uiState.value ?: return
        if (state.currentInput.isEmpty()) return
        _uiState.value = state.copy(
            currentInput = state.currentInput.dropLast(1),
            errorMessage = null,
        )
    }

    fun onSubmit() {
        val state = _uiState.value ?: return
        if (state.currentInput.length < wordLength) {
            _uiState.value = state.copy(errorMessage = "Not enough letters")
            return
        }

        when (val result = engine.submitGuess(state.currentInput)) {
            is GuessResult.Success -> {
                val newState = result.state
                _uiState.value = state.copy(
                    gameState = newState,
                    currentInput = "",
                    errorMessage = null,
                    keyboardColors = computeKeyboardColors(newState),
                )
                viewModelScope.launch {
                    if (newState.status == GameStatus.IN_PROGRESS) {
                        repository.saveInProgress(
                            wordLength,
                            puzzleIndex,
                            newState.guesses.map { it.word },
                        )
                    } else {
                        repository.clearInProgress(wordLength)
                        repository.saveCompletion(
                            wordLength,
                            puzzleIndex,
                            if (newState.status == GameStatus.WON) PuzzleResult.WIN else PuzzleResult.LOSS,
                        )
                        checkAndUnlockNextBatch()
                    }
                }
            }
            is GuessResult.Error -> {
                _uiState.value = state.copy(errorMessage = result.message)
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    fun toggleHardMode() {
        val state = _uiState.value ?: return
        // Can only toggle before first guess
        if (state.gameState.guesses.isNotEmpty()) return
        val newHardMode = !state.hardMode
        viewModelScope.launch {
            repository.setHardMode(newHardMode)
            // Recreate engine with new hard mode setting
            engine = GameEngine(
                targetWord = targetWord,
                isValidWord = { repository.isValidGuess(it, wordLength) },
                hardMode = newHardMode,
            )
            _uiState.value = state.copy(hardMode = newHardMode)
        }
    }

    private suspend fun checkAndUnlockNextBatch() {
        val count = repository.puzzleCount(wordLength)
        val puzzles = repository.puzzlesFlow(wordLength).first()
        val unlocked = puzzles.filter { it.isUnlocked }
        val allCompleted = unlocked.all {
            it.result == PuzzleResult.WIN || it.result == PuzzleResult.LOSS
        }
        if (allCompleted && unlocked.size < count) {
            repository.unlockNextBatch(wordLength)
        }
    }

    private fun computeKeyboardColors(state: GameState): Map<Char, LetterResult> {
        val map = mutableMapOf<Char, LetterResult>()
        // Priority: CORRECT > PRESENT > ABSENT
        for (guess in state.guesses) {
            guess.word.forEachIndexed { i, ch ->
                val result = guess.results[i]
                val existing = map[ch]
                if (existing == null || result.rank() > existing.rank()) {
                    map[ch] = result
                }
            }
        }
        return map
    }

    private fun LetterResult.rank() = when (this) {
        LetterResult.CORRECT -> 2
        LetterResult.PRESENT -> 1
        LetterResult.ABSENT -> 0
    }

    class Factory(
        private val repository: PuzzleRepository,
        private val wordLength: Int,
        private val puzzleIndex: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(repository, wordLength, puzzleIndex) as T
    }
}
