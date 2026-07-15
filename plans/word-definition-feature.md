# Word Definition Lookup + Voice Mode

## Context
Todo #7: "let's add a feature where clicking on a word, looks up the definition and add a voice mode where clicking on the definition reads it out loud." This is an educational feature for a kid-friendly Wordle clone -- tapping a guessed word shows its definition, and a speaker button reads it aloud.

## Pieces (each is self-contained, TDD, and resumable)

---

### Piece 1: Data model + API parsing logic (tests first)

**Test file:** `app/src/test/java/com/daphle/data/DictionaryApiClientTest.kt`
**Impl file:** `app/src/main/java/com/daphle/data/DictionaryApiClient.kt`

**Data model:**
```kotlin
data class WordDefinition(
    val partOfSpeech: String,       // "noun", "verb", "adjective", etc.
    val definition: String,
    val example: String? = null,
)
```

**Pure function to test:** `fun parseDefinitions(json: String): List<WordDefinition>`

**Definition selection rubric:**
1. Collect all meanings across ALL entries in the response JSON array
2. Group by part of speech (noun, verb, adjective, adverb)
3. For each POS, pick the meaning with the **most definitions** (proxy for most common sense)
4. Take the first definition from that winning meaning
5. Skip circular/too-short definitions (<15 chars or just restates the word) -- fall back to next definition
6. Include the `example` field when present on the chosen definition
7. Return up to 3 parts of speech max

**Tests to write:**
- Picks meaning with most definitions per POS (use "bat"-like JSON: animal=2 defs, sports=11 defs -> sports wins)
- Groups across multiple top-level entries (use "fast"-like JSON: adj in entry 0, noun in entry 1)
- Skips circular definitions (<15 chars or restates word, like "run" -> "To run.")
- Includes example when present, null when not
- Caps at 3 parts of speech
- Returns empty list for malformed JSON
- Returns empty list for empty array

**Then implement:** `parseDefinitions()` + `suspend fun lookup(word: String)` (HttpURLConnection + Dispatchers.IO)

- [x] Done

---

### Piece 2: Definition repository with caching (tests first)

**Test file:** `app/src/test/java/com/daphle/data/DefinitionRepositoryTest.kt`
**Impl file:** `app/src/main/java/com/daphle/data/DefinitionRepository.kt`

**Types:**
```kotlin
sealed class DefinitionResult {
    data class Loading(val word: String) : DefinitionResult()
    data class Success(val word: String, val definitions: List<WordDefinition>) : DefinitionResult()
    data class Error(val word: String, val message: String) : DefinitionResult()
}
```

**Tests to write:**
- Success path: calls apiClient.lookup(), returns Success with definitions
- Cache hit: second call for same word does NOT call apiClient again
- IOException from apiClient -> returns Error with kid-friendly message
- Empty definitions from apiClient -> returns Error

**Then implement:** `DefinitionRepository` with in-memory cache + mock-friendly constructor

- [x] Done

---

### Piece 3: ViewModel definition support (tests first)

**Test file:** `app/src/test/java/com/daphle/viewmodel/GameViewModelTest.kt` (modify existing)
**Impl file:** `app/src/main/java/com/daphle/viewmodel/GameViewModel.kt` (modify existing)

**Changes to `GameUiState`:** add `definitionResult: DefinitionResult? = null`
**Changes to `GameViewModel`:** add `DefinitionRepository` param, `onWordTapped(word)`, `dismissDefinition()`
**Changes to `Factory`:** add `DefinitionRepository` param

**Tests to write:**
- `onWordTapped` sets Loading then Success in uiState
- `onWordTapped` sets Loading then Error on failure
- `dismissDefinition` clears definitionResult to null

**Then implement** the ViewModel changes.

Also update `MainActivity.kt` to create `DefinitionRepository()` and pass to `GameViewModel.Factory`.

- [x] Done

---

### Piece 4: Tappable guess rows + Definition Bottom Sheet (UI)

**New file:** `app/src/main/java/com/daphle/ui/game/DefinitionBottomSheet.kt`
**Modify:** `app/src/main/java/com/daphle/ui/game/GameScreen.kt`

**GuessGrid changes:**
- Add `onWordTapped: (String) -> Unit` param
- For completed rows (`row < guesses.size`), add `Modifier.clickable` to the `Row`

**DefinitionBottomSheet:**
- Material 3 `ModalBottomSheet`
- `Loading`: word in large bold + `CircularProgressIndicator`
- `Success`: word in large bold, then for each `WordDefinition`:
  - Part of speech in italics (*noun*, *verb*, etc.)
  - Definition text
  - Example in quotes if available
  - Speaker `IconButton` to read aloud
- `Error`: word + friendly error message

**GameScreen wiring:**
- When `state.definitionResult != null`, show `DefinitionBottomSheet`
- `onDismiss = { viewModel.dismissDefinition() }`
- `onSpeak` wired to TTS (Piece 5)

- [x] Done

---

### Piece 5: Text-to-Speech + INTERNET permission

**New file:** `app/src/main/java/com/daphle/ui/game/TextToSpeechHelper.kt`
**Modify:** `app/src/main/AndroidManifest.xml`
**Modify:** `app/src/main/java/com/daphle/ui/game/GameScreen.kt`

**TTS helper:**
- `rememberTextToSpeech(): TextToSpeechState` composable
- `DisposableEffect` manages lifecycle (init on composition, shutdown on dispose)
- `speak(text)` calls `tts.speak()` with `QUEUE_FLUSH`
- `Locale.US`, speech rate ~0.85f (slower for kids)
- Expose `isReady` state; disable speaker button when not ready

**AndroidManifest:**
- Add `<uses-permission android:name="android.permission.INTERNET" />`

**GameScreen:**
- Init `val ttsState = rememberTextToSpeech()` at top
- Pass `onSpeak = { ttsState.speak(it) }` to DefinitionBottomSheet

**Manual testing:** verify TTS speaks definitions on device

- [x] Done

---

## Files Summary

| Action | File |
|--------|------|
| Modify | `app/src/main/AndroidManifest.xml` |
| Create | `app/src/main/java/com/daphle/data/DictionaryApiClient.kt` |
| Create | `app/src/main/java/com/daphle/data/DefinitionRepository.kt` |
| Modify | `app/src/main/java/com/daphle/viewmodel/GameViewModel.kt` |
| Modify | `app/src/main/java/com/daphle/MainActivity.kt` |
| Modify | `app/src/main/java/com/daphle/ui/game/GameScreen.kt` |
| Create | `app/src/main/java/com/daphle/ui/game/DefinitionBottomSheet.kt` |
| Create | `app/src/main/java/com/daphle/ui/game/TextToSpeechHelper.kt` |
| Create | `app/src/test/java/com/daphle/data/DictionaryApiClientTest.kt` |
| Create | `app/src/test/java/com/daphle/data/DefinitionRepositoryTest.kt` |
| Modify | `app/src/test/java/com/daphle/viewmodel/GameViewModelTest.kt` |
