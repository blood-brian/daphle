package com.daphle.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.daphle.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * E2E test: completing puzzle #1 then tapping puzzle #2 loads a different game.
 *
 * Word list (words_3.txt):
 *   index 0 → "the"  (Puzzle #1)
 *   index 1 → "you"  (Puzzle #2)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GameFlowE2ETest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completingPuzzle1_thenOpeningPuzzle2_showsDifferentGame() {
        // Home screen: pick 3-letter words
        composeRule.onNodeWithText("3 LETTERS").performClick()

        // Archive screen: open Puzzle #1
        composeRule.onNodeWithText("1").performClick()
        composeRule.onNodeWithText("Puzzle #1 · 3 letters").assertIsDisplayed()

        // Wait for the ViewModel to finish loading (keyboard renders after state is non-null)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("ENTER").fetchSemanticsNodes().isNotEmpty()
        }

        // Type the correct answer: T-H-E
        composeRule.onNodeWithText("T").performClick()
        composeRule.onNodeWithText("H").performClick()
        composeRule.onNodeWithText("E").performClick()
        composeRule.onNodeWithText("ENTER").performClick()

        // Win banner should appear
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("You got it! \uD83C\uDF89").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("You got it! \uD83C\uDF89").assertIsDisplayed()

        // Go back to archive
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Archive screen: open Puzzle #2
        composeRule.onNodeWithText("2").performClick()

        // Verify we're on Puzzle #2 — a different game
        composeRule.onNodeWithText("Puzzle #2 · 3 letters").assertIsDisplayed()
    }
}
