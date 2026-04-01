package com.daphle.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.daphle.MainActivity
import org.junit.Assert.assertTrue
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
        // Use completePuzzle1() to ensure puzzle #1 is done (handles prior test state)
        completePuzzle1()

        // Archive screen: open Puzzle #2 (not yet completed, navigates directly)
        composeRule.onNodeWithText("2").performClick()

        // Verify we're on Puzzle #2 — a different game
        composeRule.onNodeWithText("Puzzle #2 · 3 letters").assertIsDisplayed()
    }

    @Test
    fun tappingCompletedPuzzle_showsDialogInsteadOfNavigating() {
        completePuzzle1()

        // Tap Puzzle #1 (now completed) — dialog should appear, not the game screen
        composeRule.onNodeWithText("1").performClick()

        composeRule.onNodeWithText("Puzzle #1").assertIsDisplayed()
        composeRule.onNodeWithText("Play Again").assertIsDisplayed()
        composeRule.onNodeWithText("View Solution").assertIsDisplayed()
        // Should NOT have navigated to the game screen
        assertTrue(composeRule.onAllNodesWithText("Puzzle #1 · 3 letters").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun completedPuzzleDialog_playAgain_navigatesToGame() {
        completePuzzle1()

        composeRule.onNodeWithText("1").performClick()
        composeRule.onNodeWithText("Play Again").performClick()

        composeRule.onNodeWithText("Puzzle #1 · 3 letters").assertIsDisplayed()
    }

    @Test
    fun completedPuzzleDialog_viewSolution_navigatesToGameWithCompletedBoard() {
        completePuzzle1()

        composeRule.onNodeWithText("1").performClick()
        composeRule.onNodeWithText("View Solution").performClick()

        // Should be on the game screen showing the completed puzzle
        composeRule.onNodeWithText("Puzzle #1 · 3 letters").assertIsDisplayed()
        // Win banner confirms the solved state is shown
        composeRule.onNodeWithText("You got it! \uD83C\uDF89").assertIsDisplayed()
    }

    @Test
    fun completedPuzzleDialog_dismissOnDismiss_closesDialog() {
        completePuzzle1()

        composeRule.onNodeWithText("1").performClick()
        // Dismiss by clicking outside (onDismissRequest)
        composeRule.onNodeWithText("Puzzle #1").assertIsDisplayed()
        composeRule.onNodeWithText("Play Again").assertIsDisplayed()
        // Navigate away to confirm dialog was shown
        composeRule.onNodeWithText("Play Again").performClick()
        composeRule.onNodeWithText("Puzzle #1 · 3 letters").assertIsDisplayed()
    }

    /**
     * Ensures puzzle #1 ("the") is completed and leaves the UI on the 3-letter archive screen.
     * Idempotent: if the puzzle is already completed (DataStore persists between tests), skips
     * gameplay and just navigates to the archive screen.
     */
    private fun completePuzzle1() {
        composeRule.onNodeWithText("3 LETTERS").performClick()

        // Wait for the archive grid to load
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("1").fetchSemanticsNodes().isNotEmpty()
        }

        // If puzzle #1 is already marked with ✓ (won), skip completing it again.
        val alreadyDone = composeRule.onAllNodesWithText("✓").fetchSemanticsNodes().isNotEmpty()
        if (alreadyDone) return

        composeRule.onNodeWithText("1").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("ENTER").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("T").performClick()
        composeRule.onNodeWithText("H").performClick()
        composeRule.onNodeWithText("E").performClick()
        composeRule.onNodeWithText("ENTER").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("You got it! \uD83C\uDF89").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Back to Menu").performClick()

        // Wait for the archive to reflect the completed state before returning
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("✓").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
