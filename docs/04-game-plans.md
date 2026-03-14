# Game Plans

## 1. Shared Rules for Initial Games

Each game should define:

- local mode or multiplayer mode
- room size limits
- match start conditions
- server-authoritative actions
- end condition
- result and reward calculation
- ranking score source

## 2. Gomoku

Positioning:

- first multiplayer verification game

Players:

- 2

Core modules:

- board state
- move validator
- turn manager
- win checker
- result builder

Multiplayer flow:

- create room
- both players ready
- server assigns black and white
- server validates move order
- server broadcasts board updates
- server computes winner

## 3. Chinese Chess

Players:

- 2

Core modules:

- piece model
- board state
- move validator
- check and checkmate logic
- turn manager

Multiplayer flow:

- room and ready flow same as Gomoku
- server validates movement legality
- server handles win, resign, and timeout events

## 4. Blackjack

Players:

- 1 to N players against system dealer

Core modules:

- deck manager
- card entity
- hand evaluator
- action handler for hit and stand
- reward calculator

Multiplayer note:

- can share a table room later
- first version may start with a room-based multiplayer table or a single-player table

## 5. Doodle

Players:

- 1 or more depending on final mode

Core modules:

- canvas tool set
- stroke serializer
- replay or snapshot builder
- score rule if competition exists

Multiplayer options:

- shared drawing board
- cooperative drawing mode
- competitive timed drawing mode

## 6. Draw and Guess

Positioning:

- social multiplayer highlight game
- should be included in the first architecture design even if implementation comes after Gomoku

Players:

- recommended 3 to 8

Round model:

1. one player is assigned as drawer
2. system selects a secret word
3. drawer receives the word privately
4. drawer sends drawing strokes over WebSocket
5. guessers submit guesses in real time
6. first correct guess gets bonus score
7. round ends by correct guess or timeout
8. next round rotates drawer

Core frontend modules:

- drawing canvas
- stroke syncing
- guess input panel
- player list
- round timer
- score board

Core backend modules:

- word selector service
- drawer assignment service
- round state manager
- guess validator
- score calculator
- hidden message delivery for the drawer-only word

WebSocket message examples:

- `DRAW_STROKE`
- `DRAW_CLEAR`
- `ROUND_START`
- `ROUND_TICK`
- `GUESS_SUBMIT`
- `GUESS_RESULT`
- `PLAYER_CORRECT`
- `ROUND_END`

Important server rules:

- only the current drawer can send drawing actions
- only non-drawers can submit guesses
- the secret word must not leak to other players
- server checks correctness of guesses
- score distribution happens on the server

Recommended scoring:

- correct guesser: +score
- drawer: +score if someone guesses correctly
- faster correct guesses receive higher score
- no score for invalid or repeated spam guesses

Recommended persistence:

- round summary
- match summary
- guessed word
- winner list
- score changes

## 7. Recommended Order

1. Gomoku
2. Draw and Guess
3. Chinese Chess
4. Blackjack
5. Doodle
