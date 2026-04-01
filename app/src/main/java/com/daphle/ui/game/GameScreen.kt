package com.daphle.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daphle.game.EvaluatedGuess
import com.daphle.game.GameStatus
import com.daphle.game.LetterResult
import com.daphle.viewmodel.GameUiState
import com.daphle.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // Handle horizontal swipe to go back
    var totalDrag by remember { mutableFloatStateOf(0f) }

    // Show error messages via snackbar
    LaunchedEffect(uiState?.errorMessage) {
        val msg = uiState?.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.dismissError()
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onDragEnd = {
                    if (totalDrag > 150) { // Threshold for swipe-to-back
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }
                },
                onHorizontalDrag = { _, dragAmount ->
                    totalDrag += dragAmount
                }
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Puzzle #${viewModel.puzzleIndex + 1} · ${viewModel.wordLength} letters")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { padding ->
        val state = uiState
        if (state == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Game status banner
            if (state.gameState.status != GameStatus.IN_PROGRESS) {
                GameOverBanner(
                    won = state.gameState.status == GameStatus.WON,
                    answer = state.gameState.targetWord,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBack()
                }) {
                    Text("Back to Menu")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Guess grid
            GuessGrid(
                wordLength = viewModel.wordLength,
                maxAttempts = state.gameState.maxAttempts,
                guesses = state.gameState.guesses,
                currentInput = state.currentInput,
                currentRow = state.gameState.guesses.size,
                gameOver = state.gameState.status != GameStatus.IN_PROGRESS,
            )

            Spacer(modifier = Modifier.weight(1f))

            // QWERTY Keyboard
            QwertyKeyboard(
                keyColors = state.keyboardColors,
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onKey(it)
                },
                onBackspace = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onBackspace()
                },
                onEnter = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onSubmit()
                },
                enabled = state.gameState.status == GameStatus.IN_PROGRESS,
            )
        }
    }
}

@Composable
private fun GameOverBanner(won: Boolean, answer: String) {
    val (bg, text) = if (won) {
        Color(0xFF6AAA64) to "You got it! 🎉"
    } else {
        Color(0xFFFF6B6B) to "The word was ${answer.uppercase()}"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun GuessGrid(
    wordLength: Int,
    maxAttempts: Int,
    guesses: List<EvaluatedGuess>,
    currentInput: String,
    currentRow: Int,
    gameOver: Boolean,
) {
    val tileSize = when (wordLength) {
        3 -> 72.dp
        4 -> 64.dp
        else -> 56.dp
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(maxAttempts) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(wordLength) { col ->
                    val letter: Char?
                    val result: LetterResult?
                    when {
                        row < guesses.size -> {
                            letter = guesses[row].word[col].uppercaseChar()
                            result = guesses[row].results[col]
                        }
                        row == currentRow && !gameOver -> {
                            letter = currentInput.getOrNull(col)?.uppercaseChar()
                            result = null
                        }
                        else -> {
                            letter = null
                            result = null
                        }
                    }
                    val revealed = row < guesses.size
                    LetterTile(letter = letter, result = result, revealed = revealed, size = tileSize)
                }
            }
        }
    }
}

@Composable
private fun LetterTile(
    letter: Char?,
    result: LetterResult?,
    revealed: Boolean,
    size: Dp,
) {
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "tile_flip",
    )

    val bg = if (rotation > 90f) {
        when (result) {
            LetterResult.CORRECT -> Color(0xFF6AAA64)
            LetterResult.PRESENT -> Color(0xFFC9B458)
            LetterResult.ABSENT -> Color(0xFF787C7E)
            null -> Color.White
        }
    } else Color.White

    val textColor = if (rotation > 90f && result != null) Color.White else Color(0xFF333333)

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationX = rotation }
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(
                width = if (letter != null && !revealed) 2.dp else 1.dp,
                color = if (letter != null && !revealed) Color(0xFF888888) else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(4.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (letter != null) {
            Text(
                text = letter.toString(),
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.graphicsLayer { rotationX = if (rotation > 90f) 180f else 0f },
            )
        }
    }
}

private val KEYBOARD_ROWS = listOf(
    listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
    listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
    listOf('↵', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', '⌫'),
)

@Composable
private fun QwertyKeyboard(
    keyColors: Map<Char, LetterResult>,
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KEYBOARD_ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { ch ->
                    KeyboardKey(
                        char = ch,
                        result = if (ch.isLetter()) keyColors[ch] else null,
                        onClick = {
                            when (ch) {
                                '↵' -> onEnter()
                                '⌫' -> onBackspace()
                                else -> onKey(ch)
                            }
                        },
                        enabled = enabled,
                        isWide = ch == '↵' || ch == '⌫',
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyboardKey(
    char: Char,
    result: LetterResult?,
    onClick: () -> Unit,
    enabled: Boolean,
    isWide: Boolean,
) {
    val bg = when (result) {
        LetterResult.CORRECT -> Color(0xFF6AAA64)
        LetterResult.PRESENT -> Color(0xFFC9B458)
        LetterResult.ABSENT -> Color(0xFF787C7E)
        null -> Color(0xFFD3D6DA)
    }
    val fg = if (result != null) Color.White else Color(0xFF333333)
    val width = if (isWide) 64.dp else 34.dp

    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) bg else bg.copy(alpha = 0.5f))
            .then(
                if (enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when (char) {
                '↵' -> "ENTER"
                else -> char.toString()
            },
            fontSize = if (isWide) 12.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}
