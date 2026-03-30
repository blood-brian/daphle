package com.daphle.game

object HardModeValidator {

    /**
     * Validates a new guess against hard mode constraints from previous guesses.
     * Returns null if valid, or an error message string if invalid.
     */
    fun validate(guess: String, previousGuesses: List<EvaluatedGuess>): String? {
        val g = guess.uppercase()

        for (evaluated in previousGuesses) {
            val word = evaluated.word.uppercase()
            val results = evaluated.results

            for (i in results.indices) {
                when (results[i]) {
                    LetterResult.CORRECT -> {
                        if (g[i] != word[i]) {
                            return "hard mode: letter ${word[i]} must be in position ${i + 1}"
                        }
                    }
                    LetterResult.PRESENT -> {
                        if (word[i].uppercaseChar() !in g) {
                            return "hard mode: guess must contain letter ${word[i]}"
                        }
                    }
                    LetterResult.ABSENT -> { /* no constraint */ }
                }
            }
        }

        return null
    }
}
