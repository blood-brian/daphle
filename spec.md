# Daphle — Wordle Clone Spec

Developing a Wordle clone requires implementing core gameplay mechanics, a responsive grid interface, and state management for tracking guesses and letter accuracy. [1, 2]

---

## 1. Core Game Mechanics

- **Word Length:** The default is a 5-letter word.
- **Attempts:** The user is allowed 6 attempts to guess the word.
- **Game Loop:** The game selects a random word from a predefined dictionary at the start of each session.
- **Validation:** Every guessed word must exist in a valid dictionary (e.g., a list of 5-letter words).
- **End Conditions:** The game ends when the user correctly guesses the word or runs out of attempts. [3, 4, 5, 6, 7]

---

## 2. Feedback System (Color Coding)

The game must analyze each guess and provide immediate visual feedback:

- **Green (Correct):** The letter is in the correct position.
- **Yellow (Present):** The letter is in the word but in the wrong position.
- **Gray (Absent):** The letter is not in the word. [6, 10, 11, 12]

---

## 3. User Interface (UI)

- **Game Board:** A grid of 5×6 (5 letters, 6 rows/attempts).
- **On-Screen Keyboard:** A QWERTY-style keyboard that highlights the status of letters used across all attempts (green/yellow/gray).
- **Animations:** Flip animation for tiles upon revealing colors and a subtle shake animation for invalid words. [6, 13, 14, 15, 16]

---

## 4. Technical Stack

- **Mobile:** Native Android with Kotlin + Jetpack Compose. [3, 7, 17, 18, 19, 20]

---

## 5. Essential Features & Logic

- **State Management:** Track the current row (turn), the current letter, the grid contents, and the on-screen keyboard state.
- **Input Handling:** Listen to on-screen button clicks (and optionally physical keyboard events).
- **Word Storage:** A list of valid words for checking, and a smaller list of potential hidden answers.
- **Sharing:** A button to copy the results as emojis (e.g., 🟩⬜🟨) to the clipboard. [5, 13, 15, 21, 22]

---

## Optional Customizations

- **Word Length & Attempts:** Allow 3-letter, 4-letter, or 5-letter variations (core to Daphle).
- **Game Modes:** Implement a "Daily Word" (similar to the original) or "Unlimited/Infinite" play.
- **Difficulty:** "Hard Mode" where revealed hints must be used in subsequent guesses. [5, 16, 23, 24, 25]

---

## 6. Data Requirements

- `words_valid.txt` (or `.json`): A list of acceptable words (~10,000+).
- `words_answers.txt` (or `.json`): A smaller list of potential target words (~2,000–3,000). [6, 15]

---

## References

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
