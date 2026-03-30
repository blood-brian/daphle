package com.daphle.viewmodel

import com.daphle.data.PuzzleRepository
import com.daphle.data.PuzzleResult
import com.daphle.game.GameStatus
import com.daphle.game.LetterResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PuzzleRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads specific in-progress guesses for the given puzzle index`() = runTest {
        val wordLength = 3
        val puzzleIndex = 5
        val targetWord = "CAT"
        
        whenever(repository.answerAt(wordLength, puzzleIndex)).thenReturn(targetWord)
        whenever(repository.hardModeFlow()).thenReturn(flowOf(false))
        whenever(repository.isValidGuess("DOG", wordLength)).thenReturn(true)
        
        // Mock in-progress guesses for this specific puzzle
        whenever(repository.inProgressFlow(wordLength, puzzleIndex)).thenReturn(flowOf(listOf("DOG")))

        val viewModel = GameViewModel(repository, wordLength, puzzleIndex)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state)
        assertEquals(1, state?.gameState?.guesses?.size)
        assertEquals("DOG", state?.gameState?.guesses?.get(0)?.word)
        assertEquals(GameStatus.IN_PROGRESS, state?.gameState?.status)
    }

    @Test
    fun `starting a different puzzle index loads different guesses`() = runTest {
        val wordLength = 3
        val targetWord = "SUN"
        
        // Puzzle #1 has guesses
        whenever(repository.answerAt(wordLength, 1)).thenReturn("CAT")
        whenever(repository.inProgressFlow(wordLength, 1)).thenReturn(flowOf(listOf("DOG")))
        
        // Puzzle #2 is new (empty)
        whenever(repository.answerAt(wordLength, 2)).thenReturn("SUN")
        whenever(repository.inProgressFlow(wordLength, 2)).thenReturn(flowOf(emptyList()))
        
        whenever(repository.hardModeFlow()).thenReturn(flowOf(false))

        val viewModel = GameViewModel(repository, wordLength, 2)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(0, state?.gameState?.guesses?.size)
    }
}
