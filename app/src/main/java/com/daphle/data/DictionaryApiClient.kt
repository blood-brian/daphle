package com.daphle.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.net.HttpURLConnection
import java.net.URL

data class WordDefinition(
    val partOfSpeech: String,
    val definition: String,
    val example: String? = null,
)

class DictionaryApiClient {

    suspend fun lookup(word: String): List<WordDefinition> = withContext(Dispatchers.IO) {
        val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/${word.lowercase()}")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val json = connection.inputStream.bufferedReader().readText()
            parseDefinitions(json)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val MAX_PARTS_OF_SPEECH = 3
        private const val MIN_DEFINITION_LENGTH = 15

        fun parseDefinitions(json: String): List<WordDefinition> {
            val entries = try {
                JSONArray(json)
            } catch (_: JSONException) {
                return emptyList()
            }

            // Collect all meanings across all entries, grouped by part of speech.
            // Each group entry is a pair: (number of definitions in that meaning, the meaning JSONObject)
            data class MeaningInfo(
                val definitionCount: Int,
                val definitions: JSONArray,
            )

            val bestByPos = mutableMapOf<String, MeaningInfo>()

            for (i in 0 until entries.length()) {
                val entry = entries.optJSONObject(i) ?: continue
                val meanings = entry.optJSONArray("meanings") ?: continue

                for (j in 0 until meanings.length()) {
                    val meaning = meanings.optJSONObject(j) ?: continue
                    val pos = meaning.optString("partOfSpeech", "") .takeIf { it.isNotEmpty() } ?: continue
                    val defs = meaning.optJSONArray("definitions") ?: continue
                    val count = defs.length()

                    val existing = bestByPos[pos]
                    if (existing == null || count > existing.definitionCount) {
                        bestByPos[pos] = MeaningInfo(count, defs)
                    }
                }
            }

            // For each POS, pick the first non-circular, non-short definition
            val results = mutableListOf<WordDefinition>()
            for ((pos, info) in bestByPos) {
                if (results.size >= MAX_PARTS_OF_SPEECH) break

                for (k in 0 until info.definitions.length()) {
                    val defObj = info.definitions.optJSONObject(k) ?: continue
                    val defText = defObj.optString("definition", "")
                    if (defText.length < MIN_DEFINITION_LENGTH) continue

                    val example = defObj.optString("example", null)
                        .takeIf { !it.isNullOrEmpty() }

                    results.add(WordDefinition(pos, defText, example))
                    break
                }
            }

            return results
        }
    }
}
