package com.daphle.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daphle.data.DefinitionResult
import com.daphle.data.WordDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinitionBottomSheet(
    result: DefinitionResult,
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            when (result) {
                is DefinitionResult.Loading -> LoadingContent(result.word)
                is DefinitionResult.Success -> SuccessContent(result.word, result.definitions, onSpeak)
                is DefinitionResult.Error -> ErrorContent(result.word, result.message)
            }
        }
    }
}

@Composable
private fun LoadingContent(word: String) {
    WordHeader(word)
    Spacer(modifier = Modifier.height(16.dp))
    CircularProgressIndicator(
        modifier = Modifier.size(32.dp),
    )
}

@Composable
private fun SuccessContent(
    word: String,
    definitions: List<WordDefinition>,
    onSpeak: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WordHeader(word)
        IconButton(
            onClick = { onSpeak(buildSpeechText(word, definitions)) },
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Read aloud",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    definitions.forEach { def ->
        Text(
            text = def.partOfSpeech,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = def.definition,
            fontSize = 18.sp,
        )
        if (def.example != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${def.example}\"",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ErrorContent(word: String, message: String) {
    WordHeader(word)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = message,
        fontSize = 16.sp,
        color = Color.Gray,
    )
}

@Composable
private fun WordHeader(word: String) {
    Text(
        text = word.uppercase(),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
    )
}

private fun buildSpeechText(word: String, definitions: List<WordDefinition>): String {
    val parts = mutableListOf(word)
    definitions.forEach { def ->
        parts.add("${def.partOfSpeech}. ${def.definition}")
        if (def.example != null) {
            parts.add("For example: ${def.example}")
        }
    }
    return parts.joinToString(". ")
}
