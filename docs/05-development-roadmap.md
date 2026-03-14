# Development Roadmap

## 1. Current Planning Output

This document is the execution order for the next development phase.

Planning documents:

- `00-project-overview.md`
- `01-system-architecture.md`
- `02-backend-class-design.md`
- `03-frontend-script-design.md`
- `04-game-plans.md`

## 2. Stage 1: Project Skeleton

Backend:

- initialize Spring Boot project
- create base packages
- create common response and exception classes
- create security skeleton
- create module package placeholders

Player web:

- initialize Vue 3 + Vite project
- create router, stores, layouts, and shared API client
- create game registry and base game interfaces

Admin web:

- initialize Vue 3 + Vite project
- create login layout and admin routing
- create user, asset, and game management placeholders

## 3. Stage 2: Core Platform Services

- implement registration and login
- implement JWT auth flow
- implement user asset and asset log services
- implement game list and game config services
- implement admin game management and asset adjustment

## 4. Stage 3: Multiplayer Foundation

- implement room tables and room APIs
- implement WebSocket connection management
- implement room ready flow
- implement match creation and match state storage
- implement generic rule engine interface

## 5. Stage 4: First Game Delivery

- implement Gomoku frontend and rule engine
- connect Gomoku room and match flow
- persist Gomoku records and rankings
- validate the full loop from room to reward settlement

## 6. Stage 5: Social Multiplayer Game

- implement Draw and Guess room rules
- implement hidden word distribution
- implement drawing stroke sync and guess validation
- implement scoring and round rotation

## 7. Stage 6: Additional Games

- implement Chinese Chess
- implement Blackjack
- implement Doodle

## 8. Suggested Immediate Tasks

1. Create the backend project skeleton.
2. Create the player web and admin web skeletons.
3. Create the database design document and SQL draft.
4. Create interface placeholder classes from the backend and frontend design docs.
