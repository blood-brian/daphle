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
 * - Completion state per word (WIN / LOSS / NONE)
 * - In-progress guesses per word
 * - Unlocked batch index per word length
 * - Hard mode setting
 */
class GameProgressStore(private val context: Context) {

    companion object {
        const val BATCH_SIZE = 10
        private val HARD_MODE_KEY = stringPreferencesKey("hard_mode")

        private fun completionKey(word: String) =
            stringPreferencesKey("completion_${word.uppercase()}")

        private fun inProgressGuessesKey(word: String) =
            stringPreferencesKey("in_progress_guesses_${word.uppercase()}")

        private fun completedGuessesKey(word: String) =
            stringPreferencesKey("completed_guesses_${word.uppercase()}")

        private fun unlockedBatchKey(length: Int) =
            intPreferencesKey("unlocked_batch_$length")
    }

    // ----- Completion state -----

    suspend fun setCompletion(word: String, result: PuzzleResult) {
        context.dataStore.edit { prefs ->
            prefs[completionKey(word)] = result.name
        }
    }

    fun completionFlow(word: String): Flow<PuzzleResult> =
        context.dataStore.data.map { prefs ->
            prefs[completionKey(word)]?.let { PuzzleResult.valueOf(it) }
                ?: PuzzleResult.NONE
        }

    /** Returns a flow of completion results for a list of words. */
    fun allCompletionsFlow(words: List<String>): Flow<List<PuzzleResult>> =
        context.dataStore.data.map { prefs ->
            words.map { word ->
                val completion = prefs[completionKey(word)]?.let { PuzzleResult.valueOf(it) }
                if (completion != null && completion != PuzzleResult.NONE) {
                    completion
                } else {
                    // If not completed, check if it's in progress
                    if (prefs.contains(inProgressGuessesKey(word))) {
                        PuzzleResult.IN_PROGRESS
                    } else {
                        PuzzleResult.NONE
                    }
                }
            }
        }

    // ----- Completed game guesses -----

    suspend fun saveCompletedGuesses(word: String, guesses: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[completedGuessesKey(word)] = guesses.joinToString(",")
        }
    }

    fun completedGuessesFlow(word: String): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedGuessesKey(word)] ?: return@map emptyList()
            if (raw.isBlank()) emptyList() else raw.split(",")
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

    suspend fun saveInProgress(word: String, guesses: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[inProgressGuessesKey(word)] = guesses.joinToString(",")
        }
    }

    suspend fun clearInProgress(word: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(inProgressGuessesKey(word))
        }
    }

    fun inProgressFlow(word: String): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val guessesRaw = prefs[inProgressGuessesKey(word)] ?: return@map emptyList()
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
