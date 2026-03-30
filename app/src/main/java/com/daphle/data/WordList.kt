package com.daphle.data

import android.content.Context

class WordList(context: Context) {

    private val answers3: List<String> by lazy { loadAsset(context, "words_3.txt") }
    private val answers4: List<String> by lazy { loadAsset(context, "words_4.txt") }
    private val answers5: List<String> by lazy { loadAsset(context, "words_5.txt") }

    private val guesses3: Set<String> by lazy { loadAsset(context, "guesses_3.txt").toHashSet() }
    private val guesses4: Set<String> by lazy { loadAsset(context, "guesses_4.txt").toHashSet() }
    private val guesses5: Set<String> by lazy { loadAsset(context, "guesses_5.txt").toHashSet() }

    fun answersForLength(length: Int): List<String> = when (length) {
        3 -> answers3
        4 -> answers4
        5 -> answers5
        else -> emptyList()
    }

    fun isValidGuess(word: String, length: Int): Boolean {
        val w = word.lowercase()
        return when (length) {
            3 -> w in guesses3
            4 -> w in guesses4
            5 -> w in guesses5
            else -> false
        }
    }

    private fun loadAsset(context: Context, filename: String): List<String> =
        context.assets.open(filename)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
            .map { it.trim().lowercase() }
}
