package com.daphle.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daphle.viewmodel.HomeViewModel

private val RainbowColors = listOf(
    Color(0xFFFF595E), // Red
    Color(0xFFFF924C), // Orange
    Color(0xFFFFCA3A), // Yellow
    Color(0xFF8AC926), // Green
    Color(0xFF1982C4), // Blue
    Color(0xFF6A4C93)  // Purple
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPickLength: (Int) -> Unit
) {
    val hardMode by viewModel.hardMode.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA), // Very light gray background to make colors pop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Rainbow Title
            Row {
                "Daphle".forEachIndexed { index, char ->
                    Text(
                        text = char.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RainbowColors[index % RainbowColors.size]
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pick a word length to play!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Colorful Hard Mode Toggle
            Button(
                onClick = { viewModel.toggleHardMode() },
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hardMode) Color(0xFF6AAA64) else Color(0xFF787C7E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (hardMode) "HARD MODE: ON ✓" else "HARD MODE: OFF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(3, 4, 5).forEachIndexed { index, length ->
                    LengthSelector(
                        length = length,
                        startIndex = if (length == 3) 0 else if (length == 4) 2 else 4,
                        onClick = { onPickLength(length) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LengthSelector(length: Int, startIndex: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(length) { i ->
                    val colorIndex = (startIndex + i) % RainbowColors.size
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RainbowColors[colorIndex].copy(alpha = 0.15f))
                            .border(3.dp, RainbowColors[colorIndex], RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$length LETTERS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF333333)
            )
        }
    }
}
