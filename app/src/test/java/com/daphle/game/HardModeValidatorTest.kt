package com.daphle.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HardModeValidatorTest {

    @Nested
    inner class FirstGuess {
        @Test
        fun `first guess is always valid with no prior hints`() {
            val result = HardModeValidator.validate("CRANE", emptyList())
            assertNull(result, "First guess should always be valid")
        }
    }

    @Nested
    inner class GreenConstraints {
        @Test
        fun `must reuse green letter in same position`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.CORRECT,   // C at 0
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                )
            )
            // COULD has C at position 0 -- should pass
            val result = HardModeValidator.validate("COULD", previousGuesses)
            assertNull(result, "Guess with C at position 0 should be valid")
        }

        @Test
        fun `violating green constraint returns error`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.CORRECT,   // C at 0
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                )
            )
            // TRAIN does not have C at position 0
            val result = HardModeValidator.validate("TRAIN", previousGuesses)
            assertNotNull(result, "Guess without C at position 0 should fail")
            assertTrue(result!!.contains("C"), "Error should mention the letter C: $result")
            assertTrue(result.contains("position 1"), "Error should mention position (1-indexed): $result")
        }

        @Test
        fun `multiple green letters enforced`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.CORRECT,   // C at 0
                        LetterResult.ABSENT,
                        LetterResult.CORRECT,   // A at 2
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                )
            )
            // Must have C at 0 AND A at 2
            // "CHAFF" has C at 0 and A at 2 -- wait, C-H-A-F-F, A is at index 2. Good.
            val validResult = HardModeValidator.validate("CHAFF", previousGuesses)
            assertNull(validResult, "Guess with C at 0 and A at 2 should be valid")

            // "COULD" has C at 0 but not A at 2
            val invalidResult = HardModeValidator.validate("COULD", previousGuesses)
            assertNotNull(invalidResult, "Guess without A at position 2 should fail")
        }
    }

    @Nested
    inner class YellowConstraints {
        @Test
        fun `must include yellow letter somewhere`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.ABSENT,
                        LetterResult.PRESENT,   // R is present
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                )
            )
            // RADIO contains R -- should pass
            val result = HardModeValidator.validate("RADIO", previousGuesses)
            assertNull(result, "Guess containing R should be valid")
        }

        @Test
        fun `violating yellow constraint returns error`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.ABSENT,
                        LetterResult.PRESENT,   // R is present
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                )
            )
            // TULIP does not contain R
            val result = HardModeValidator.validate("TULIP", previousGuesses)
            assertNotNull(result, "Guess without R should fail")
            assertTrue(result!!.contains("R"), "Error should mention the letter R: $result")
        }

        @Test
        fun `multiple yellow letters enforced`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.ABSENT,
                        LetterResult.PRESENT,   // R is present
                        LetterResult.ABSENT,
                        LetterResult.PRESENT,   // N is present
                        LetterResult.ABSENT
                    )
                )
            )
            // RINDS contains both R and N -- should pass
            val result = HardModeValidator.validate("RINDS", previousGuesses)
            assertNull(result, "Guess containing both R and N should be valid")

            // RIDGE contains R but not N -- should fail
            val invalidResult = HardModeValidator.validate("RIDGE", previousGuesses)
            assertNotNull(invalidResult, "Guess missing N should fail")
            assertTrue(invalidResult!!.contains("N"), "Error should mention missing letter N: $invalidResult")
        }
    }

    @Nested
    inner class CombinedConstraints {
        @Test
        fun `green and yellow constraints from multiple guesses are all enforced`() {
            val previousGuesses = listOf(
                EvaluatedGuess(
                    word = "CRANE",
                    results = listOf(
                        LetterResult.CORRECT,   // C at 0
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.ABSENT
                    )
                ),
                EvaluatedGuess(
                    word = "COULD",
                    results = listOf(
                        LetterResult.CORRECT,   // C at 0
                        LetterResult.ABSENT,
                        LetterResult.ABSENT,
                        LetterResult.PRESENT,   // L is present
                        LetterResult.ABSENT
                    )
                )
            )
            // Must have C at 0 AND contain L somewhere
            // "CLIPS" = C at 0, has L -- should pass
            val validResult = HardModeValidator.validate("CLIPS", previousGuesses)
            assertNull(validResult, "Guess with C at 0 and containing L should be valid")

            // "CHAMP" = C at 0, but no L -- should fail
            val invalidResult = HardModeValidator.validate("CHAMP", previousGuesses)
            assertNotNull(invalidResult, "Guess missing L should fail")
        }
    }
}
