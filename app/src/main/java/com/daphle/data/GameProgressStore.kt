package com.daphle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daphle_progress")

/**
 * Persists:
 * - Completion state per puzzle (WIN / LOSS / NONE)
 * - In-progress guesses per puzzle
 * - Unlocked batch index per word length
 * - Hard mode setting
 */
class GameProgressStore(private val context: Context) {

    companion object {
        const val BATCH_SIZE = 10
        private val HARD_MODE_KEY = stringPreferencesKey("hard_mode")

        private fun completionKey(length: Int, puzzleIndex: Int) =
            stringPreferencesKey("completion_${length}_$puzzleIndex")

        private fun inProgressGuessesKey(length: Int, puzzleIndex: Int) =
            stringPreferencesKey("in_progress_guesses_${length}_$puzzleIndex")

        private fun unlockedBatchKey(length: Int) =
            intPreferencesKey("unlocked_batch_$length")
    }

    // ----- Completion state -----

    suspend fun setCompletion(length: Int, puzzleIndex: Int, result: PuzzleResult) {
        context.dataStore.edit { prefs ->
            prefs[completionKey(length, puzzleIndex)] = result.name
        }
    }

    fun completionFlow(length: Int, puzzleIndex: Int): Flow<PuzzleResult> =
        context.dataStore.data.map { prefs ->
            prefs[completionKey(length, puzzleIndex)]?.let { PuzzleResult.valueOf(it) }
                ?: PuzzleResult.NONE
        }

    /** Returns a flow of completion results for all puzzles of a given length up to [count]. */
    fun allCompletionsFlow(length: Int, count: Int): Flow<List<PuzzleResult>> =
        context.dataStore.data.map { prefs ->
            (0 until count).map { i ->
                val completion = prefs[completionKey(length, i)]?.let { PuzzleResult.valueOf(it) }
                if (completion != null && completion != PuzzleResult.NONE) {
                    completion
                } else {
                    // If not completed, check if it's in progress
                    if (prefs.contains(inProgressGuessesKey(length, i))) {
                        PuzzleResult.IN_PROGRESS
                    } else {
                        PuzzleResult.NONE
                    }
                }
            }
        }

    // ----- Unlocked batches -----

    fun unlockedBatchFlow(length: Int): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[unlockedBatchKey(length)] ?: 0
        }

    suspend fun unlockNextBatch(length: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[unlockedBatchKey(length)] ?: 0
            prefs[unlockedBatchKey(length)] = current + 1
        }
    }

    // ----- In-progress games -----

    suspend fun saveInProgress(length: Int, puzzleIndex: Int, guesses: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[inProgressGuessesKey(length, puzzleIndex)] = guesses.joinToString(",")
        }
    }

    suspend fun clearInProgress(length: Int, puzzleIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs.remove(inProgressGuessesKey(length, puzzleIndex))
        }
    }

    fun inProgressFlow(length: Int, puzzleIndex: Int): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val guessesRaw = prefs[inProgressGuessesKey(length, puzzleIndex)] ?: return@map emptyList()
            if (guessesRaw.isBlank()) emptyList() else guessesRaw.split(",")
        }

    // ----- Hard mode -----

    fun hardModeFlow(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[HARD_MODE_KEY] == "true"
        }

    suspend fun setHardMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HARD_MODE_KEY] = enabled.toString()
        }
    }
}

enum class PuzzleResult { NONE, IN_PROGRESS, WIN, LOSS }
