package com.daphle.viewmodel

import com.daphle.data.PuzzleRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ArchiveViewModelTest {

    private val repository: PuzzleRepository = mock()

    @Test
    fun `answerAt delegates to repository with the correct word length`() {
        val wordLength = 3
        whenever(repository.puzzlesFlow(wordLength)).thenReturn(flowOf(emptyList()))
        whenever(repository.answerAt(wordLength, 0)).thenReturn("the")

        val viewModel = ArchiveViewModel(repository, wordLength)

        assertEquals("the", viewModel.answerAt(0))
    }

    @Test
    fun `answerAt returns correct word for given puzzle index`() {
        val wordLength = 4
        whenever(repository.puzzlesFlow(wordLength)).thenReturn(flowOf(emptyList()))
        whenever(repository.answerAt(wordLength, 7)).thenReturn("frog")

        val viewModel = ArchiveViewModel(repository, wordLength)

        assertEquals("frog", viewModel.answerAt(7))
    }
}
