package com.daphle.data

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class DefinitionRepositoryTest {

    private val mockClient = mock<DictionaryApiClient>()
    private val repo = DefinitionRepository(mockClient)

    // -------------------------------------------------------
    // 1. Success path
    // -------------------------------------------------------
    @Test
    fun `returns Success with definitions from api client`() = runTest {
        val defs = listOf(
            WordDefinition("noun", "A small furry animal.", "The cat sat on the mat."),
        )
        whenever(mockClient.lookup("cat")).thenReturn(defs)

        val result = repo.getDefinition("cat")

        assertTrue(result is DefinitionResult.Success)
        val success = result as DefinitionResult.Success
        assertEquals("cat", success.word)
        assertEquals(defs, success.definitions)
    }

    // -------------------------------------------------------
    // 2. Cache hit
    // -------------------------------------------------------
    @Test
    fun `second call for same word uses cache and does not call api again`() = runTest {
        val defs = listOf(WordDefinition("noun", "A covering for the head."))
        whenever(mockClient.lookup("hat")).thenReturn(defs)

        repo.getDefinition("hat")
        val result = repo.getDefinition("hat")

        verify(mockClient).lookup("hat") // called exactly once
        assertTrue(result is DefinitionResult.Success)
    }

    // -------------------------------------------------------
    // 3. IOException -> Error
    // -------------------------------------------------------
    @Test
    fun `returns Error with friendly message on IOException`() = runTest {
        whenever(mockClient.lookup("dog")).doAnswer { throw IOException("no internet") }

        val result = repo.getDefinition("dog")

        assertTrue(result is DefinitionResult.Error)
        val error = result as DefinitionResult.Error
        assertEquals("dog", error.word)
        assertTrue(error.message.contains("internet", ignoreCase = true))
    }

    // -------------------------------------------------------
    // 4. Empty definitions -> Error
    // -------------------------------------------------------
    @Test
    fun `returns Error when api returns empty definitions`() = runTest {
        whenever(mockClient.lookup("xyz")).thenReturn(emptyList())

        val result = repo.getDefinition("xyz")

        assertTrue(result is DefinitionResult.Error)
        val error = result as DefinitionResult.Error
        assertEquals("xyz", error.word)
    }

    // -------------------------------------------------------
    // 5. Does not cache errors
    // -------------------------------------------------------
    @Test
    fun `does not cache errors so retry hits api again`() = runTest {
        var callCount = 0
        whenever(mockClient.lookup("dog")).doAnswer {
            callCount++
            if (callCount == 1) throw IOException("no internet")
            listOf(WordDefinition("noun", "A domesticated canine mammal."))
        }

        val first = repo.getDefinition("dog")
        assertTrue(first is DefinitionResult.Error)

        val second = repo.getDefinition("dog")
        assertTrue(second is DefinitionResult.Success)
    }
}
