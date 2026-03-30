package com.daphle.game

data class EvaluatedGuess(
    val word: String,
    val results: List<LetterResult>
)

enum class GameStatus { IN_PROGRESS, WON, LOST }

data class GameState(
    val targetWord: String,
    val wordLength: Int,
    val maxAttempts: Int,
    val guesses: List<EvaluatedGuess> = emptyList(),
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val hardMode: Boolean = false
)

sealed class GuessResult {
    data class Success(val state: GameState) : GuessResult()
    data class Error(val message: String) : GuessResult()
}
