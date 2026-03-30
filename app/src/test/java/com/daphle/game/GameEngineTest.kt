package com.daphle.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GameEngineTest {

    private val allWordsValid: (String) -> Boolean = { true }
    private val noWordsValid: (String) -> Boolean = { false }

    @Nested
    inner class InitialState {
        @Test
        fun `new game has correct initial state with zero guesses and IN_PROGRESS`() {
            val engine = GameEngine("HELLO", allWordsValid)
            val state = engine.currentState

            assertEquals(0, state.guesses.size)
            assertEquals(GameStatus.IN_PROGRESS, state.status)
            assertEquals("HELLO", state.targetWord)
            assertEquals(5, state.wordLength)
            assertEquals(6, state.maxAttempts)
            assertFalse(state.hardMode)
        }
    }

    @Nested
    inner class MaxAttemptsByWordLength {
        @Test
        fun `3-letter word gets 4 attempts`() {
            val engine = GameEngine("CAT", allWordsValid)
            assertEquals(4, engine.currentState.maxAttempts)
            assertEquals(3, engine.currentState.wordLength)
        }

        @Test
        fun `4-letter word gets 5 attempts`() {
            val engine = GameEngine("BARK", allWordsValid)
            assertEquals(5, engine.currentState.maxAttempts)
            assertEquals(4, engine.currentState.wordLength)
        }

        @Test
        fun `5-letter word gets 6 attempts`() {
            val engine = GameEngine("CRANE", allWordsValid)
            assertEquals(6, engine.currentState.maxAttempts)
            assertEquals(5, engine.currentState.wordLength)
        }
    }

    @Nested
    inner class ValidGuesses {
        @Test
        fun `valid guess is accepted and evaluation is added to state`() {
            val engine = GameEngine("CRANE", allWordsValid)
            val result = engine.submitGuess("TRAIN")

            assertTrue(result is GuessResult.Success)
            val state = (result as GuessResult.Success).state
            assertEquals(1, state.guesses.size)
            assertEquals("TRAIN", state.guesses[0].word)
            assertEquals(5, state.guesses[0].results.size)
            assertEquals(GameStatus.IN_PROGRESS, state.status)
        }

        @Test
        fun `correct guess changes status to WON`() {
            val engine = GameEngine("CRANE", allWordsValid)
            val result = engine.submitGuess("CRANE")

            assertTrue(result is GuessResult.Success)
            val state = (result as GuessResult.Success).state
            assertEquals(GameStatus.WON, state.status)
            assertTrue(state.guesses[0].results.all { it == LetterResult.CORRECT })
        }

        @Test
        fun `game tracks all guesses in order`() {
            val engine = GameEngine("CRANE", allWordsValid)
            engine.submitGuess("TRAIN")
            engine.submitGuess("PLANE")
            val result = engine.submitGuess("BRACE")

            assertTrue(result is GuessResult.Success)
            val state = (result as GuessResult.Success).state
            assertEquals(3, state.guesses.size)
            assertEquals("TRAIN", state.guesses[0].word)
            assertEquals("PLANE", state.guesses[1].word)
            assertEquals("BRACE", state.guesses[2].word)
        }
    }

    @Nested
    inner class GameOver {
        @Test
        fun `exhaust all attempts without correct guess results in LOST`() {
            val engine = GameEngine("CAT", allWordsValid)  // 3 letters = 4 attempts
            engine.submitGuess("DOG")
            engine.submitGuess("BAT")
            engine.submitGuess("RAT")
            val result = engine.submitGuess("HAT")

            assertTrue(result is GuessResult.Success)
            val state = (result as GuessResult.Success).state
            assertEquals(GameStatus.LOST, state.status)
            assertEquals(4, state.guesses.size)
        }

        @Test
        fun `reject guess after game is won`() {
            val engine = GameEngine("CAT", allWordsValid)
            engine.submitGuess("CAT")  // Win
            val result = engine.submitGuess("DOG")

            assertTrue(result is GuessResult.Error)
            assertEquals("Game is already over", (result as GuessResult.Error).message)
        }

        @Test
        fun `reject guess after game is lost`() {
            val engine = GameEngine("CAT", allWordsValid)
            engine.submitGuess("DOG")
            engine.submitGuess("BAT")
            engine.submitGuess("RAT")
            engine.submitGuess("HAT")  // 4th attempt, game lost
            val result = engine.submitGuess("MAT")

            assertTrue(result is GuessResult.Error)
            assertEquals("Game is already over", (result as GuessResult.Error).message)
        }
    }

    @Nested
    inner class InvalidGuesses {
        @Test
        fun `reject invalid word when isValidWord returns false`() {
            val engine = GameEngine("CRANE", noWordsValid)
            val result = engine.submitGuess("XXXXX")

            assertTrue(result is GuessResult.Error)
            assertEquals("Not a valid word", (result as GuessResult.Error).message)
        }
    }

    @Nested
    inner class HardMode {
        @Test
        fun `hard mode flag is reflected in game state`() {
            val engine = GameEngine("CRANE", allWordsValid, hardMode = true)
            assertTrue(engine.currentState.hardMode)
        }

        @Test
        fun `hard mode rejects guess that violates green constraint`() {
            val engine = GameEngine("CRANE", allWordsValid, hardMode = true)
            // Guess CRANE-like word that reveals C in position 0
            engine.submitGuess("COULD")  // C is correct at position 0
            // Next guess must have C at position 0
            val result = engine.submitGuess("TRAIN")  // No C at position 0

            assertTrue(result is GuessResult.Error)
            val error = (result as GuessResult.Error).message
            assertTrue(error.contains("hard mode"), "Error should mention hard mode: $error")
        }

        @Test
        fun `hard mode rejects guess that violates yellow constraint`() {
            // Target: CRANE
            // Guess "RECAL" -- wait, let me think about this more carefully
            // Target: CRANE. Guess "NACRE" -> N:PRESENT, A:PRESENT, C:PRESENT, R:PRESENT, E:PRESENT
            // All letters present but wrong positions. Next guess must contain N, A, C, R, E somewhere.
            val engine = GameEngine("CRANE", allWordsValid, hardMode = true)
            engine.submitGuess("NACRE")  // All letters present but rearranged
            // Next guess "BLANK" is missing several required letters
            val result = engine.submitGuess("BLANK")

            assertTrue(result is GuessResult.Error)
            val error = (result as GuessResult.Error).message
            assertTrue(error.contains("hard mode"), "Error should mention hard mode: $error")
        }
    }
}
