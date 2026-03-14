# WebGame Project Overview

## 1. Project Goal

Build a multi-game web platform that supports:

- multiple casual games under one site
- user registration, login, and authorization
- coins, score, rewards, and rankings
- multiplayer rooms and real-time play via WebSocket
- an admin console for game, player, and asset management
- future expansion for new games without major refactoring

Initial game set:

- Gomoku
- Chinese Chess
- Poker game(s), starting with Blackjack
- Doodle mini-game
- Draw and Guess

## 2. Tech Stack

Backend:

- Spring Boot
- Spring Security
- JWT
- MyBatis-Plus
- MySQL
- Redis (optional but recommended for sessions, room cache, rankings)
- WebSocket

Frontend:

- Vue 3
- Vite
- Vue Router
- Pinia
- Canvas for game rendering where appropriate
- static HTML/JS pages for games that do not fit the Vue runtime well

Admin:

- Vue 3
- Vite
- Vue Router
- Pinia

## 3. Product Scope

Player-side capabilities:

- browse game lobby
- view game details and status
- create or join multiplayer rooms
- play games in local or multiplayer mode
- receive score and coin rewards
- view personal records and rankings

Admin-side capabilities:

- manage users
- adjust user score and coins
- enable or disable games
- edit game configs
- review room, match, and record data
- manage announcements and operations

## 4. Core Platform Principles

- game-independent platform framework
- each game plugs into a shared registry and shared lifecycle
- room and match logic are reusable across games
- the server is the authority for multiplayer validation
- reward and ranking systems are standardized

## 5. Development Milestones

Phase 1:

- create backend skeleton
- create player web skeleton
- create admin web skeleton
- complete auth and user asset basics

Phase 2:

- implement game registry and game SDK
- implement room and WebSocket framework
- implement admin asset and game management

Phase 3:

- launch Gomoku as the first fully connected multiplayer game
- validate room, match, reward, and ranking flows

Phase 4:

- add Chinese Chess
- add Blackjack
- add Doodle
- add Draw and Guess

Phase 5:

- optimize user experience
- add events, notices, and more game content
