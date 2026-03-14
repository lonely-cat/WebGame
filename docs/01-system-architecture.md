# System Architecture

## 1. High-Level Structure

Project modules:

- `backend` for APIs, auth, game platform, rooms, matches, ranking, and admin services
- `player-web` for the player-facing website and game container
- `admin-web` for the operator dashboard
- `docs` for architecture and development planning

## 2. Runtime Flow

HTTP responsibilities:

- authentication
- profile loading
- game list and configuration
- room creation and room detail queries
- historical record queries
- ranking queries
- admin management actions

WebSocket responsibilities:

- room presence
- ready status
- match start
- real-time player actions
- game state synchronization
- heartbeat and reconnection hints
- match end broadcast

## 3. Recommended Project Layout

```text
WebGame/
  docs/
  backend/
    src/main/java/com/webgame/
      common/
      security/
      websocket/
      modules/
        auth/
        user/
        asset/
        game/
        room/
        match/
        record/
        rank/
        admin/
  player-web/
    src/
      api/
      components/
      games/
      layouts/
      router/
      sdk/
      stores/
      utils/
      views/
      websocket/
  admin-web/
    src/
      api/
      components/
      layouts/
      router/
      stores/
      views/
```

## 4. Multiplayer Model

Core concepts:

- `room`: players gather and prepare here
- `match`: active game session derived from a room
- `game rule engine`: validates and applies game actions
- `state snapshot`: a serializable view of current game state
- `message dispatcher`: routes socket events to room or match logic

Lifecycle:

1. player authenticates by HTTP
2. player creates or joins a room by HTTP
3. player connects to WebSocket using authenticated identity
4. room events are exchanged through WebSocket
5. server starts a match once start conditions are met
6. players send actions via WebSocket
7. server validates actions through the rule engine
8. server broadcasts updated state
9. server computes result, rewards assets, persists records, and updates rankings

## 5. Server-Side Design Rules

- keep game-specific rules out of controllers
- keep room orchestration and match orchestration separate
- treat the backend as the final authority for multiplayer games
- use DTOs for API input and output, not entities directly
- keep reward and ranking updates transactional

## 6. Frontend Design Rules

- all games must register through a shared game registry
- all games should implement a common lifecycle interface
- multiplayer games should extend a common multiplayer base class
- routing should not depend on individual game internals
- player asset and profile info should stay in shared stores

## 7. Future Extension Points

- AI opponents for board games
- tournament mode
- spectator mode
- seasonal rankings
- social features
- voice or chat in rooms
