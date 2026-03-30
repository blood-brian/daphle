package com.daphle.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GuessEvaluatorTest {

    // Helper to make test assertions more readable
    private fun assertEvaluation(
        guess: String,
        target: String,
        expected: List<LetterResult>,
    ) {
        val result = GuessEvaluator.evaluate(guess, target)
        assertEquals(expected, result, "evaluate(\"$guess\", \"$target\")")
    }

    // -------------------------------------------------------
    // 1. All correct (exact match)
    // -------------------------------------------------------
    @Test
    fun `all letters correct for exact match`() {
        assertEvaluation(
            guess = "CRANE",
            target = "CRANE",
            expected = listOf(
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
            ),
        )
    }

    // -------------------------------------------------------
    // 2. All absent (no letters match)
    // -------------------------------------------------------
    @Test
    fun `all letters absent when no letters match`() {
        assertEvaluation(
            guess = "BRICK",
            target = "STUDY",
            expected = listOf(
                LetterResult.ABSENT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
            ),
        )
    }

    // -------------------------------------------------------
    // 3. Mix of correct, present, absent
    // -------------------------------------------------------
    @Test
    fun `mix of correct present and absent`() {
        // guess=CRANE, target=CHARM
        // C: correct (C==C), R: present (R in target at pos 3), A: correct (A==A), N: absent, E: absent
        assertEvaluation(
            guess = "CRANE",
            target = "CHARM",
            expected = listOf(
                LetterResult.CORRECT,
                LetterResult.PRESENT,
                LetterResult.CORRECT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
            ),
        )
    }

    // -------------------------------------------------------
    // 4. Duplicate letter in guess, single in target
    //    guess="SPEED", target="ABIDE"
    //    S:ABSENT, P:ABSENT, E:ABSENT, E:PRESENT, D:PRESENT
    //    target has one E at index 4. Neither E in guess is at
    //    index 4, so no CORRECT. The second E (index 3) gets
    //    PRESENT; the first E (index 2) is excess -> ABSENT.
    // -------------------------------------------------------
    @Test
    fun `duplicate letter in guess single in target - excess is absent`() {
        // target=ABIDE: A,B,I,D,E (one E at index 4)
        // guess=SPEED:  S,P,E,E,D
        // Pass 1: no exact matches
        // Pass 2 left-to-right: E[2] claims the one E -> PRESENT, E[3] -> no E left -> ABSENT
        assertEvaluation(
            guess = "SPEED",
            target = "ABIDE",
            expected = listOf(
                LetterResult.ABSENT,  // S
                LetterResult.ABSENT,  // P
                LetterResult.PRESENT, // E (claims the one available E)
                LetterResult.ABSENT,  // E (excess, no more E's)
                LetterResult.PRESENT, // D
            ),
        )
    }

    // -------------------------------------------------------
    // 5. Duplicate letter in guess, both correct
    //    guess="CREEP", target="CREEK"
    //    C:CORRECT, R:CORRECT, E:CORRECT, E:CORRECT, P:ABSENT
    //    target=CREEK -> C,R,E,E,K  (two E's)
    //    guess=CREEP  -> C,R,E,E,P
    // -------------------------------------------------------
    @Test
    fun `duplicate letter in guess both match correctly`() {
        assertEvaluation(
            guess = "CREEP",
            target = "CREEK",
            expected = listOf(
                LetterResult.CORRECT, // C
                LetterResult.CORRECT, // R
                LetterResult.CORRECT, // E
                LetterResult.CORRECT, // E
                LetterResult.ABSENT,  // P
            ),
        )
    }

    // -------------------------------------------------------
    // 6. Duplicate letter in guess, one correct one excess
    //    guess="ALLOT", target="ULTRA"
    //    target has one L (index 1? no... U,L,T,R,A)
    //    A: present (A is in target at index 4, not index 0)
    //    L: CORRECT (L at index 1 matches target index 1)
    //    L: ABSENT  (only one L in target, already used)
    //    O: ABSENT
    //    T: PRESENT (T is in target at index 2)
    // -------------------------------------------------------
    @Test
    fun `duplicate letter one correct one excess - correct takes priority`() {
        // target=ULTRA: U,L,T,R,A (one L at index 1)
        // guess=ALLOT:  A,L,L,O,T
        // Pass 1 (correct): A[0]!=U, L[1]==L[1] CORRECT, L[2]!=T, O[3]!=R, T[4]!=A
        // Remaining target counts: U:1, T:1, R:1, A:1 (L already consumed)
        // Pass 2: A[0] -> A in remaining -> PRESENT. L[2] -> no L remaining -> ABSENT.
        //   O[3] -> not in target -> ABSENT. T[4] -> T in remaining -> PRESENT.
        assertEvaluation(
            guess = "ALLOT",
            target = "ULTRA",
            expected = listOf(
                LetterResult.PRESENT, // A (in target at index 4)
                LetterResult.CORRECT, // L (matches target index 1)
                LetterResult.ABSENT,  // L (only one L, already used)
                LetterResult.ABSENT,  // O
                LetterResult.PRESENT, // T (in target at index 2)
            ),
        )
    }

    // -------------------------------------------------------
    // 7. Case insensitivity
    // -------------------------------------------------------
    @Test
    fun `case insensitive comparison`() {
        assertEvaluation(
            guess = "Hello",
            target = "hello",
            expected = listOf(
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
            ),
        )
    }

    @Test
    fun `case insensitive comparison mixed case`() {
        assertEvaluation(
            guess = "HeLLo",
            target = "HELLO",
            expected = listOf(
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
            ),
        )
    }

    // -------------------------------------------------------
    // 8. Three-letter words
    // -------------------------------------------------------
    @Test
    fun `three letter word all correct`() {
        assertEvaluation(
            guess = "CAT",
            target = "CAT",
            expected = listOf(
                LetterResult.CORRECT,
                LetterResult.CORRECT,
                LetterResult.CORRECT,
            ),
        )
    }

    @Test
    fun `three letter word mixed results`() {
        // guess=CAT, target=ACT -> C:PRESENT, A:PRESENT, T:CORRECT
        assertEvaluation(
            guess = "CAT",
            target = "ACT",
            expected = listOf(
                LetterResult.PRESENT,  // C
                LetterResult.PRESENT,  // A
                LetterResult.CORRECT,  // T
            ),
        )
    }

    // -------------------------------------------------------
    // 9. Four-letter words
    // -------------------------------------------------------
    @Test
    fun `four letter word all absent`() {
        assertEvaluation(
            guess = "FISH",
            target = "GUNK",
            expected = listOf(
                LetterResult.ABSENT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
                LetterResult.ABSENT,
            ),
        )
    }

    @Test
    fun `four letter word mixed results`() {
        // guess=BARN, target=BRAN -> B:CORRECT, A:PRESENT, R:PRESENT, N:CORRECT
        assertEvaluation(
            guess = "BARN",
            target = "BRAN",
            expected = listOf(
                LetterResult.CORRECT, // B
                LetterResult.PRESENT, // A
                LetterResult.PRESENT, // R
                LetterResult.CORRECT, // N
            ),
        )
    }

    // -------------------------------------------------------
    // 10. Five-letter words (additional coverage)
    // -------------------------------------------------------
    @Test
    fun `five letter word with present letters`() {
        // guess=ADIEU, target=AUDIO
        // A:CORRECT, D:PRESENT, I:PRESENT, E:ABSENT, U:PRESENT
        assertEvaluation(
            guess = "ADIEU",
            target = "AUDIO",
            expected = listOf(
                LetterResult.CORRECT, // A
                LetterResult.PRESENT, // D
                LetterResult.PRESENT, // I
                LetterResult.ABSENT,  // E
                LetterResult.PRESENT, // U
            ),
        )
    }

    @Test
    fun `five letter word duplicate in guess one correct one present`() {
        // guess=GEESE, target="LEDGE"
        // target: L,E,D,G,E  (E at indices 1 and 4, G at index 3)
        // guess:  G,E,E,S,E
        // Pass 1 (correct matches): G[0]!=L, E[1]==E[1] CORRECT, E[2]!=D, S[3]!=G, E[4]==E[4] CORRECT
        // Remaining target letters: L(0), D(2), G(3) -> no more E's available
        // Pass 2: G[0] -> G is in remaining {L,D,G} -> PRESENT. S[3] -> not in remaining -> ABSENT. E[2] -> no E remaining -> ABSENT.
        assertEvaluation(
            guess = "GEESE",
            target = "LEDGE",
            expected = listOf(
                LetterResult.PRESENT, // G (G is in target at index 3)
                LetterResult.CORRECT, // E (matches target index 1)
                LetterResult.ABSENT,  // E (no more E's available)
                LetterResult.ABSENT,  // S
                LetterResult.CORRECT, // E (matches target index 4)
            ),
        )
    }

    @Test
    fun `triple letter in guess only one in target`() {
        // guess=EERIE, target=STEAL
        // target: S,T,E,A,L (one E at index 2)
        // Pass 1: E[0]!=S, E[1]!=T, R[2]!=E, I[3]!=A, E[4]!=L -> no correct
        // Remaining target letter counts: S:1, T:1, E:1, A:1, L:1
        // Pass 2 left-to-right: E[0] -> E in remaining (count 1) -> PRESENT, remaining E count -> 0
        //   E[1] -> E count is 0 -> ABSENT
        //   R[2] -> not in target -> ABSENT
        //   I[3] -> not in target -> ABSENT
        //   E[4] -> E count is 0 -> ABSENT
        assertEvaluation(
            guess = "EERIE",
            target = "STEAL",
            expected = listOf(
                LetterResult.PRESENT, // E (claims the one E)
                LetterResult.ABSENT,  // E (no more E's)
                LetterResult.ABSENT,  // R
                LetterResult.ABSENT,  // I
                LetterResult.ABSENT,  // E (no more E's)
            ),
        )
    }
}
