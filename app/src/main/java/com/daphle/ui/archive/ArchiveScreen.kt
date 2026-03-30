package com.daphle.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daphle.data.PuzzleInfo
import com.daphle.data.PuzzleResult
import com.daphle.viewmodel.ArchiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onBack: () -> Unit,
    onPuzzleTap: (puzzleIndex: Int) -> Unit,
) {
    val puzzles by viewModel.puzzles.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Handle horizontal swipe to go back
    var totalDrag by remember { mutableFloatStateOf(0f) }

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
                title = { Text("${viewModel.wordLength}-letter puzzles") },
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
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(puzzles) { puzzle ->
                PuzzleTile(
                    puzzle = puzzle,
                    onClick = { if (puzzle.isUnlocked) onPuzzleTap(puzzle.index) },
                )
            }
        }
    }
}

@Composable
private fun PuzzleTile(puzzle: PuzzleInfo, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val (bg, fg, icon) = when {
        !puzzle.isUnlocked -> Triple(Color(0xFFE0E0E0), Color(0xFF9E9E9E), "🔒")
        puzzle.result == PuzzleResult.WIN -> Triple(Color(0xFF6AAA64), Color.White, "✓")
        puzzle.result == PuzzleResult.LOSS -> Triple(Color(0xFFFF6B6B), Color.White, "✗")
        puzzle.result == PuzzleResult.IN_PROGRESS -> Triple(Color(0xFFC9B458), Color.White, "…")
        else -> Triple(Color.White, Color(0xFF333333), null)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = if (!puzzle.isUnlocked) 0.dp else 1.dp,
                color = Color(0xFFDDDDDD),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = puzzle.isUnlocked) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Text(text = icon, fontSize = 16.sp)
            }
            Text(
                text = "${puzzle.index + 1}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
        }
    }
}
