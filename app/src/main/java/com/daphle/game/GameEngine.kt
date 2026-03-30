package com.daphle.game

class GameEngine(
    targetWord: String,
    private val isValidWord: (String) -> Boolean,
    hardMode: Boolean = false
) {
    private val target = targetWord.uppercase()
    private val wordLength = target.length
    private val maxAttempts = when (wordLength) {
        3 -> 4
        4 -> 5
        else -> 6
    }

    var currentState = GameState(
        targetWord = target,
        wordLength = wordLength,
        maxAttempts = maxAttempts,
        hardMode = hardMode
    )
        private set

    fun submitGuess(guess: String): GuessResult {
        val state = currentState

        if (state.status != GameStatus.IN_PROGRESS) {
            return GuessResult.Error("Game is already over")
        }

        val normalizedGuess = guess.uppercase()

        if (!isValidWord(normalizedGuess)) {
            return GuessResult.Error("Not a valid word")
        }

        if (state.hardMode) {
            val violation = HardModeValidator.validate(normalizedGuess, state.guesses)
            if (violation != null) {
                return GuessResult.Error(violation)
            }
        }

        val results = GuessEvaluator.evaluate(normalizedGuess, target)
        val evaluatedGuess = EvaluatedGuess(word = normalizedGuess, results = results)
        val newGuesses = state.guesses + evaluatedGuess

        val won = results.all { it == LetterResult.CORRECT }
        val lost = !won && newGuesses.size >= maxAttempts

        val newStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        currentState = state.copy(
            guesses = newGuesses,
            status = newStatus
        )

        return GuessResult.Success(currentState)
    }
}
