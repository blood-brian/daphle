package com.daphle.game

enum class LetterResult { CORRECT, PRESENT, ABSENT }

object GuessEvaluator {

    /**
     * Evaluates a [guess] against a [target] word and returns a [LetterResult] for each position.
     *
     * The algorithm uses two passes:
     * 1. First pass: identify all CORRECT (exact position) matches and count remaining
     *    unmatched target letters.
     * 2. Second pass: for non-correct positions, check if the letter exists in the
     *    remaining target letter pool. If so, mark PRESENT and decrement the pool;
     *    otherwise mark ABSENT.
     *
     * This ensures correct matches are always prioritized over present matches when
     * handling duplicate letters.
     */
    fun evaluate(guess: String, target: String): List<LetterResult> {
        val g = guess.lowercase()
        val t = target.lowercase()

        val results = Array(g.length) { LetterResult.ABSENT }

        // Count of each unmatched target letter (after removing exact matches)
        val remainingCounts = mutableMapOf<Char, Int>()
        for (ch in t) {
            remainingCounts[ch] = (remainingCounts[ch] ?: 0) + 1
        }

        // Pass 1: Mark CORRECT matches and reduce remaining counts
        for (i in g.indices) {
            if (g[i] == t[i]) {
                results[i] = LetterResult.CORRECT
                remainingCounts[g[i]] = remainingCounts[g[i]]!! - 1
            }
        }

        // Pass 2: Mark PRESENT or ABSENT for non-correct positions
        for (i in g.indices) {
            if (results[i] == LetterResult.CORRECT) continue

            val count = remainingCounts[g[i]] ?: 0
            if (count > 0) {
                results[i] = LetterResult.PRESENT
                remainingCounts[g[i]] = count - 1
            }
            // else stays ABSENT
        }

        return results.toList()
    }
}
