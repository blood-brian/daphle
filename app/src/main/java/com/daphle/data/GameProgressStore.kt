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
 * - Completion state per puzzle (WIN / LOSS / IN_PROGRESS / NONE)
 * - In-progress guesses for the current puzzle per word length
 * - Unlocked batch index per word length
 * - Hard mode setting
 */
class GameProgressStore(private val context: Context) {

    companion object {
        const val BATCH_SIZE = 10
        private val HARD_MODE_KEY = stringPreferencesKey("hard_mode")

        private fun completionKey(length: Int, puzzleIndex: Int) =
            stringPreferencesKey("completion_${length}_$puzzleIndex")

        private fun inProgressGuessesKey(length: Int) =
            stringPreferencesKey("in_progress_guesses_$length")

        private fun inProgressIndexKey(length: Int) =
            intPreferencesKey("in_progress_index_$length")

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
                prefs[completionKey(length, i)]?.let { PuzzleResult.valueOf(it) }
                    ?: PuzzleResult.NONE
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

    // ----- In-progress game -----

    suspend fun saveInProgress(length: Int, puzzleIndex: Int, guesses: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[inProgressIndexKey(length)] = puzzleIndex
            prefs[inProgressGuessesKey(length)] = guesses.joinToString(",")
        }
    }

    suspend fun clearInProgress(length: Int) {
        context.dataStore.edit { prefs ->
            prefs.remove(inProgressIndexKey(length))
            prefs.remove(inProgressGuessesKey(length))
        }
    }

    fun inProgressFlow(length: Int): Flow<InProgressGame?> =
        context.dataStore.data.map { prefs ->
            val index = prefs[inProgressIndexKey(length)] ?: return@map null
            val guessesRaw = prefs[inProgressGuessesKey(length)] ?: return@map null
            val guesses = if (guessesRaw.isBlank()) emptyList() else guessesRaw.split(",")
            InProgressGame(puzzleIndex = index, guesses = guesses)
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

data class InProgressGame(val puzzleIndex: Int, val guesses: List<String>)
