package com.daphle.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PuzzleInfo(
    val index: Int,
    val wordLength: Int,
    val result: PuzzleResult,
    val isUnlocked: Boolean,
)

class PuzzleRepository(context: Context) {

    private val wordList = WordList(context)
    private val store = GameProgressStore(context)

    fun puzzlesFlow(length: Int): Flow<List<PuzzleInfo>> {
        val answers = wordList.answersForLength(length)
        return combine(
            store.allCompletionsFlow(answers),
            store.unlockedBatchFlow(length),
        ) { completions, unlockedBatch ->
            val unlockedCount = (unlockedBatch + 1) * GameProgressStore.BATCH_SIZE

            completions.mapIndexed { i, result ->
                PuzzleInfo(
                    index = i,
                    wordLength = length,
                    result = result,
                    isUnlocked = i < unlockedCount,
                )
            }
        }
    }

    fun answerAt(length: Int, index: Int): String =
        wordList.answersForLength(length)[index]

    fun isValidGuess(word: String, length: Int): Boolean =
        wordList.isValidGuess(word, length)

    suspend fun saveCompletion(length: Int, index: Int, result: PuzzleResult) {
        val word = answerAt(length, index)
        store.setCompletion(word, result)
    }

    suspend fun saveInProgress(length: Int, index: Int, guesses: List<String>) {
        val word = answerAt(length, index)
        store.saveInProgress(word, guesses)
    }

    suspend fun clearInProgress(length: Int, index: Int) {
        val word = answerAt(length, index)
        store.clearInProgress(word)
    }

    fun inProgressFlow(length: Int, index: Int): Flow<List<String>> {
        val word = answerAt(length, index)
        return store.inProgressFlow(word)
    }

    fun unlockedBatchFlow(length: Int) = store.unlockedBatchFlow(length)

    suspend fun unlockNextBatch(length: Int) = store.unlockNextBatch(length)

    fun puzzleCount(length: Int) = wordList.answersForLength(length).size

    fun hardModeFlow() = store.hardModeFlow()

    suspend fun setHardMode(enabled: Boolean) = store.setHardMode(enabled)
}
