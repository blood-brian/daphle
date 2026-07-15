package com.daphle.ui.game

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class TextToSpeechState {
    var isReady by mutableStateOf(false)
        internal set
    internal var tts: TextToSpeech? = null

    fun speak(text: String) {
        if (!isReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "definition")
    }

    internal fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}

@Composable
fun rememberTextToSpeech(): TextToSpeechState {
    val context = LocalContext.current
    val state = remember { TextToSpeechState() }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                state.tts?.let { tts ->
                    tts.language = Locale.US
                    tts.setSpeechRate(0.85f)
                    tts.setPitch(1.1f)

                    // Pick the best available American English voice:
                    // prefer en_US, high-quality, offline voices
                    val voices = tts.voices?.filter { !it.isNetworkConnectionRequired }
                    val usVoices = voices?.filter { it.locale == Locale.US }
                    val fallback = voices?.filter { it.locale.language == "en" }
                    val best = (usVoices?.sortedByDescending { it.quality }
                        ?: fallback?.sortedByDescending { it.quality })
                    best?.firstOrNull()?.let { voice ->
                        tts.voice = voice
                    }

                    state.isReady = true
                }
            }
        }
        state.tts = tts

        onDispose {
            state.shutdown()
        }
    }

    return state
}
