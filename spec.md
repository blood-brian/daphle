# Daphle — Wordle Clone Spec

Developing a Wordle clone requires implementing core gameplay mechanics, a responsive grid interface, and state management for tracking guesses and letter accuracy. This is for a 4-year old who loves Wordle. She wants to play a 3 and 4 letter variant.
---

## 1. Core Game Mechanics

- **Word Length:** The default is a 5-letter word.
- **Attempts:** The user is allowed 6 attempts to guess the word.
- **Game Loop:** The game selects a random word from a predefined dictionary at the start of each session.
- **Validation:** Every guessed word must exist in a valid dictionary (e.g., a list of 5-letter words).
- **End Conditions:** The game ends when the user correctly guesses the word or runs out of attempts.

---

## 2. Feedback System (Color Coding)

The game must analyze each guess and provide immediate visual feedback:

- **Green (Correct):** The letter is in the correct position.
- **Yellow (Present):** The letter is in the word but in the wrong position.
- **Gray (Absent):** The letter is not in the word.

---

## 3. User Interface (UI)

- **Game Board:** A grid of 5×6 (5 letters, 6 rows/attempts).
- **On-Screen Keyboard:** A QWERTY-style keyboard that highlights the status of letters used across all attempts (green/yellow/gray).
- **Animations:** Flip animation for tiles upon revealing colors and a subtle shake animation for invalid words.

---

## 4. Technical Stack

- **Mobile:** Native Android with Kotlin + Jetpack Compose.

---

## 5. Architecture — Clean Architecture + MVVM

The most maintainable approach is Clean Architecture combined with MVVM. This allows swapping the UI or storage layer without touching game logic. [A1, A2]

### Key Modules (Gradle-based Separation)

- **`:core:domain` (The Brain):** Pure Kotlin, zero Android dependencies. Contains the `WordleGame` class, guess validation logic, and repository interfaces.
- **`:core:data` (The Library):** Implements dictionary and storage logic. Depends on `:domain` and handles fetching words from assets or a remote API.
- **`:feature:game` (The UI):** Contains the ViewModel and Jetpack Compose UI. Communicates with the domain layer only via Use Cases. [A2, A4, A5, A6]

### Information Flow

1. **User Action:** Player taps a key on the on-screen keyboard.
2. **ViewModel:** UI calls `viewModel.submitGuess()`.
3. **Use Case (Domain):** ViewModel triggers `SubmitGuessUseCase`, which asks the Repository Interface to validate the word.
4. **Repository (Data):** The implementation (in `:data`) checks the word list.
5. **State Update:** If valid, the Use Case updates `GameState` (an immutable Kotlin `StateFlow`).
6. **Persistence:** The Repository saves the new state via Room or DataStore.
7. **Reactive UI:** The UI, observing the `StateFlow`, automatically updates the grid and plays animations. [A2]

### Repository Pattern for Persistence

**Interface (in `:domain`):**
```kotlin
interface GameRepository {
    suspend fun saveGame(state: GameState)
    suspend fun getGame(): GameState?
}
```

**Implementation (in `:data`):** Start with `SharedPrefsRepository`. Later, swap in a `RoomRepository` via Hilt/Koin with no changes to UI or logic code. [A2, A8]

### Recommended Project Structure

```
project-root/
├── app/                   (glue code)
├── core/
│   ├── domain/            (entities, use cases, repository interfaces)
│   └── data/              (Room DB, repository implementations)
└── features/
    └── game/              (Compose UI, ViewModels)
```

---

## 6. Essential Features & Logic

- **State Management:** Track the current row (turn), the current letter, the grid contents, and the on-screen keyboard state.
- **Input Handling:** Listen to on-screen button clicks (and optionally physical keyboard events).
- **Word Storage:** A list of valid words for checking, and a smaller list of potential hidden answers.

---

## Optional Customizations

- **Word Length & Attempts:** Allow 3-letter, 4-letter, or 5-letter variations (core to Daphle).
- **Game Modes:** Implement a "Daily Word" (similar to the original) or "Unlimited/Infinite" play.
- **Difficulty:** "Hard Mode" where revealed hints must be used in subsequent guesses. [5, 16, 23, 24, 25]

---

## 6. Data Requirements

- `words_valid.txt` (or `.json`): A list of acceptable words (~10,000+).
- `words_answers.txt` (or `.json`): A smaller list of potential target words (~2,000–3,000). 
-  We should find through web search or create a list of the most common 3, 4, and 5-letter words to use in crating these lists.
-  We can also look at language learning, and learning-to-read apps for lists of words. Some of the current 5-letter words in the current Wordle answer list are a bit esoteric for a 4-year old.

## References

### Architecture
- [A1] https://github.com/vmadalin/android-modular-architecture
- [A2] https://medium.com/droidstack/clean-architecture-in-android-advanced-guide-614637cd25b3
- [A3] https://developer.android.com/studio/projects
- [A4] https://developer.android.com/topic/architecture/recommendations
- [A5] https://proandroiddev.com/from-monolith-to-modules-modernizing-your-android-app-architecture-2f99338e8d27
- [A6] https://abifarhan.medium.com/modularization-in-android-development-architecting-for-scale-in-high-traffic-apps-214cf92d6a08
- [A7] https://dev.to/artsiom_seliuzhytski/modularisation-in-android-engineering-scalable-maintainable-projects-3ff0
- [A8] https://proandroiddev.com/android-components-architecture-in-a-modular-word-7414a0631969

### Wordle / Game Design
1. https://www.osiztechnologies.com/blog/wordle-clone-script
2. https://thecodingchannel.hashnode.dev/full-tutorial-we-build-a-python-wordle-clone-discord-bot-with-disnake
3. https://realpython.com/python-wordle-clone/
4. https://www.freecodecamp.org/news/building-a-wordle-game/
5. https://www.osiztechnologies.com/blog/wordle-clone-script
6. https://medium.com/strategio/build-a-wordle-clone-in-java-c7b7b924fb8d
7. https://www.freecodecamp.org/news/how-to-build-a-wordle-clone-using-python-and-rich/
8. https://www.freecodecamp.org/news/build-a-wordle-clone-in-javascript/
9. https://itnext.io/building-a-wordle-clone-using-outsystems-bd705fe6971
10. https://www.cnet.com/tech/gaming/wordle-on-game-boy-is-fun-but-is-it-wordle-anymore/
11. https://en.wikipedia.org/wiki/Wordle
12. https://snap.berkeley.edu/project?user=helicoptur&project=Wordle
13. https://www.youtube.com/watch?v=ZSWl5UwhHcs
14. https://medium.com/@bgw26/wordle-clone-using-javascript-5593da330891
15. https://www.reddit.com/r/wordle/comments/us3236/question_how_to_people_just_create_new_wordle/
16. https://github.com/KSukher/Wordle-Clone
17. https://www.reddit.com/r/AskProgrammers/comments/ybd3u5/tips_on_building_a_worldle_clone_with_0_experience/
18. https://www.reactnativeschool.com/build-a-wordle-clone-with-react-native/
19. https://dev.to/nexxeln/i-made-a-wordle-clone-1h9d
20. https://www.reddit.com/r/androiddev/comments/vz4cxn/i_made_a_wordle_clone_open_source/
21. https://www.reddit.com/r/nocode/comments/1ge9kxm/building_a_wordle_clone_in_30min_with_ai_no/
22. https://www.reddit.com/r/react/comments/xvbok7/complexity_level_of_wordle_clone/
23. https://www.youtube.com/watch?v=eCddp53N63E
24. https://github.com/eternalthinker/wordle-clone
25. https://www.siliconrepublic.com/business/wordle-online-game-twitter
