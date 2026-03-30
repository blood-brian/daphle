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
        val count = answers.size
        return combine(
            store.allCompletionsFlow(length, count),
            store.unlockedBatchFlow(length),
        ) { completions, unlockedBatch ->
            val unlockedCount = (unlockedBatch + 1) * GameProgressStore.BATCH_SIZE

            // Check if we should unlock the next batch: all puzzles in current batch completed
            val currentBatchEnd = minOf(unlockedCount, count)
            val currentBatchStart = unlockedBatch * GameProgressStore.BATCH_SIZE
            // (unlocking is triggered from ArchiveViewModel when needed)

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
        store.setCompletion(length, index, result)
    }

    suspend fun saveInProgress(length: Int, index: Int, guesses: List<String>) {
        store.saveInProgress(length, index, guesses)
    }

    suspend fun clearInProgress(length: Int) {
        store.clearInProgress(length)
    }

    fun inProgressFlow(length: Int) = store.inProgressFlow(length)

    fun unlockedBatchFlow(length: Int) = store.unlockedBatchFlow(length)

    suspend fun unlockNextBatch(length: Int) = store.unlockNextBatch(length)

    fun puzzleCount(length: Int) = wordList.answersForLength(length).size

    fun hardModeFlow() = store.hardModeFlow()

    suspend fun setHardMode(enabled: Boolean) = store.setHardMode(enabled)
}
