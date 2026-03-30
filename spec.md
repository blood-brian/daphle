# Daphle — Kid-Friendly Wordle Spec

A Wordle-style word guessing game for a 4-year-old who loves Wordle and is good with real words. Built as a native Android app with Kotlin + Jetpack Compose.

---

## 1. Core Game Mechanics

- **Word Lengths:** 3, 4, and 5 letters — all available from v1.
- **Attempts:** 6 attempts for all word lengths.
- **Validation:** Every guess must be a real word from a kid-friendly word list. Invalid words are rejected with a shake animation.
- **Feedback (Color Coding):**
  - **Green (Correct):** Letter is in the correct position.
  - **Yellow (Present):** Letter is in the word but wrong position.
  - **Gray (Absent):** Letter is not in the word.
  - Duplicate letter handling: each letter in the guess is evaluated against the count of that letter in the answer. Excess duplicates are marked gray.
- **End Conditions:** The game ends when the player guesses correctly (win) or exhausts all attempts (loss).

---

## 2. Game Flow

```
Home Screen → Pick word length (3 / 4 / 5)
  → Archive Grid (list of puzzles for that length)
    → Tap a puzzle → Play Game Screen
      → Win or lose → Back to Archive Grid
```

1. **Home Screen:** Three buttons to pick word length (3, 4, or 5 letters).
2. **Archive Grid:** Shows all puzzles for the selected word length. Each puzzle is a numbered tile showing its status: locked, available, in-progress, or completed (with green/red for win/loss).
3. **Game Screen:** The player guesses the word. On completion, they return to the archive grid.

There is no "daily word" or "free play" mode. All gameplay is through the archive.

---

## 3. Archive Grid & Progression

- Each word length has a fixed, ordered list of answer words (the puzzle archive), ordered from most common/familiar to least common — so early puzzles are easier.
- **Batch Unlocking:** Puzzles unlock in batches (e.g., 10 at a time). The first batch is unlocked from the start. When all puzzles in a batch are completed, the next batch unlocks.
- **Puzzle States:**
  - 🔒 **Locked** — not yet unlocked
  - ⬜ **Available** — unlocked but not started
  - 🟡 **In Progress** — started but not finished
  - ✅ **Completed (Win)** — solved
  - ❌ **Completed (Loss)** — failed (all attempts used)
- **Replay/Reset:** Completed puzzles (win or loss) can be replayed. Resetting clears the current attempt state and lets the player try again. The completion status resets when replayed.

---

## 4. User Interface

### Game Board
- Dynamic grid sized to word length and attempts: 6 rows for all word lengths.
- Tiles flip to reveal color feedback after each guess.
- Invalid word → shake animation on the current row.

### On-Screen Keyboard
- Standard QWERTY layout.
- Keys reflect cumulative letter status across all guesses (green > yellow > gray).
- Backspace and Enter keys included.

### Home Screen
- Clean, simple layout with three large buttons for word length selection (3, 4, 5).
- Kid-friendly design — large touch targets, clear typography.

### Archive Grid
- Scrollable grid of numbered puzzle tiles for the selected word length.
- Visual distinction between locked, available, in-progress, and completed states.
- Tap an available or completed puzzle to play or replay it.

---

## 5. Hard Mode

- Available as a toggle (accessible from game screen or settings).
- **Rules:** Any letter revealed as green must stay in that position in subsequent guesses. Any letter revealed as yellow must be included somewhere in subsequent guesses.
- Hard mode state is per-game (can be toggled before the first guess, locked after).

---

## 6. Architecture

Single `app` module. No multi-module structure, no DI framework, no repository interfaces.

### Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **State Management:** ViewModel + StateFlow
- **Persistence:** DataStore (Preferences) for game progress and archive state
- **Navigation:** Compose Navigation (Home → Archive → Game)

### Structure
```
app/src/main/java/com/daphle/
├── MainActivity.kt
├── ui/
│   ├── theme/Theme.kt
│   ├── home/HomeScreen.kt
│   ├── archive/ArchiveScreen.kt
│   └── game/GameScreen.kt
├── game/
│   ├── GuessEvaluator.kt       // Green/Yellow/Gray logic
│   ├── GameState.kt            // State data classes
│   ├── GameEngine.kt           // Game rules, state transitions
│   └── HardModeValidator.kt    // Hard mode constraint checking
├── data/
│   ├── WordList.kt             // Load & query word lists from assets
│   ├── PuzzleRepository.kt     // Archive state, puzzle progress
│   └── GameProgressStore.kt    // DataStore persistence
└── viewmodel/
    ├── HomeViewModel.kt
    ├── ArchiveViewModel.kt
    └── GameViewModel.kt
```

### Information Flow
1. Player taps a key on the on-screen keyboard.
2. ViewModel updates the current guess in GameState.
3. On Enter: GameEngine validates the word (via WordList), evaluates the guess (via GuessEvaluator), and updates GameState.
4. StateFlow emits new state → Compose UI reacts.
5. On game end: PuzzleRepository persists the completion status.

---

## 7. Word Lists

### Files
Stored in `app/src/main/assets/`:
- `words_3.txt` — 3-letter answer pool
- `words_4.txt` — 4-letter answer pool
- `words_5.txt` — 5-letter answer pool
- `guesses_3.txt` — valid 3-letter guesses (superset of answers)
- `guesses_4.txt` — valid 4-letter guesses (superset of answers)
- `guesses_5.txt` — valid 5-letter guesses (superset of answers)

### Sourcing & Ordering
- Answer pools: curated from early-reader word lists, phonics/sight word lists, and common vocabulary for ages 4–7. Words should be concrete, familiar nouns, verbs, and adjectives a young child would know.
- **Answer pool ordering:** Words are ordered from most common/familiar to least common. Puzzles are played in this order, so the earliest puzzles use the simplest, most recognizable words (e.g., "cat", "dog", "sun") and difficulty gradually increases.
- Valid guesses: broader set of real English words at each length, so the game accepts words the child might try even if they're not in the answer pool. Guess lists are sorted alphabetically for efficient lookup.
- All words stored lowercase, one per line.

---

## 8. Persistence

Using Jetpack DataStore (Preferences):

- **Archive State:** For each word length, track which puzzles are completed (win/loss) and which batch is unlocked.
- **In-Progress Game:** Save current puzzle ID, guesses made so far, and current row — so the player can leave and come back.
- **Settings:** Hard mode toggle state.

No Room database. No remote API.

---

## 9. Development Approach — TDD

All game logic is built test-first. For each feature:
1. Write failing unit tests that define expected behavior.
2. Implement minimum code to pass.
3. Refactor.

### Test Priorities (in order)
1. **Guess evaluation** — Green/Yellow/Gray results, duplicate letter edge cases.
2. **Word validation** — reject non-words, accept valid words, case insensitivity.
3. **Game state transitions** — correct guess → win, exhaust attempts → loss, track current row, enforce attempt limits per word length.
4. **Hard mode enforcement** — confirmed green/yellow letters must be reused.
5. **Archive/progression** — batch unlocking logic, replay/reset, persistence of completed state.
6. **ViewModel integration** — StateFlow emissions match expected UI state after each action.

### Test Tooling
- JUnit 5 for unit tests (pure Kotlin, no Android dependencies for game logic)
- Domain logic lives in pure Kotlin classes testable without Robolectric
- Compose UI testing deferred to after core logic is solid

---

## 10. V2 Ideas (Not in V1)

- Sound effects (letter tap, win fanfare, etc.)
- Confetti animation on win
- Sharing / clipboard emoji export
- Statistics screen (win rate, streak, guess distribution)
- Hint system (reveal a letter)

---

## 11. Technical Stack Summary

| Component | Choice |
|-----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow |
| Persistence | DataStore (Preferences) |
| Navigation | Compose Navigation |
| Testing | JUnit 5 |
| Min SDK | 24 |
| Target SDK | 34 |
