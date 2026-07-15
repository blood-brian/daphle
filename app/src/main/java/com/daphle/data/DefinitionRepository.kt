package com.daphle.data

import java.io.IOException

sealed class DefinitionResult {
    data class Loading(val word: String) : DefinitionResult()
    data class Success(val word: String, val definitions: List<WordDefinition>) : DefinitionResult()
    data class Error(val word: String, val message: String) : DefinitionResult()
}

class DefinitionRepository(
    private val apiClient: DictionaryApiClient = DictionaryApiClient(),
) {
    private val cache = mutableMapOf<String, DefinitionResult.Success>()

    suspend fun getDefinition(word: String): DefinitionResult {
        cache[word]?.let { return it }

        return try {
            val definitions = apiClient.lookup(word)
            if (definitions.isEmpty()) {
                DefinitionResult.Error(word, "No definition found for this word.")
            } else {
                val success = DefinitionResult.Success(word, definitions)
                cache[word] = success
                success
            }
        } catch (_: IOException) {
            DefinitionResult.Error(word, "Could not look up this word. Check your internet connection!")
        }
    }
}
