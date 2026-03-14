# Frontend Script Design

## 1. Frontend Applications

- `player-web`: player-facing site and all games
- `admin-web`: admin dashboard

## 2. Player Web Directory Structure

```text
player-web/src/
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
```

## 3. API Layer

### `api/authApi.ts`
Methods:

- `login(data)`
- `register(data)`
- `refreshToken()`
- `getProfile()`

### `api/gameApi.ts`
Methods:

- `getGameList()`
- `getGameDetail(gameCode)`
- `getGameConfig(gameCode)`

### `api/roomApi.ts`
Methods:

- `createRoom(data)`
- `joinRoom(roomCode)`
- `leaveRoom(roomId)`
- `ready(roomId)`
- `cancelReady(roomId)`
- `getRoomDetail(roomCode)`

### `api/recordApi.ts`
Methods:

- `getMyRecords(params)`
- `getRank(gameCode)`

### `api/assetApi.ts`
Methods:

- `getMyAsset()`
- `getAssetLogs()`

## 4. State Stores

### `stores/useAuthStore.ts`
State:

- `token`
- `userInfo`

Methods:

- `login()`
- `logout()`
- `fetchProfile()`

### `stores/useGameStore.ts`
State:

- `gameList`
- `currentGame`
- `gameConfig`

Methods:

- `loadGames()`
- `loadGameDetail(gameCode)`
- `loadGameConfig(gameCode)`

### `stores/useRoomStore.ts`
State:

- `currentRoom`
- `roomPlayers`
- `readyMap`

Methods:

- `createRoom()`
- `joinRoom()`
- `leaveRoom()`
- `setReady()`

### `stores/useAssetStore.ts`
State:

- `coin`
- `score`

Methods:

- `fetchMyAsset()`
- `refreshAsset()`

### `stores/useWsStore.ts`
State:

- `connected`
- `lastMessage`

Methods:

- `connect()`
- `disconnect()`
- `sendMessage()`

## 5. WebSocket Client Layer

### `websocket/GameSocketClient.ts`
Methods:

- `connect(token)`
- `disconnect()`
- `send(message)`
- `onMessage(handler)`
- `onOpen(handler)`
- `onClose(handler)`
- `heartbeat()`

### `websocket/RoomChannel.ts`
Methods:

- `joinRoom(roomCode)`
- `leaveRoom(roomCode)`
- `ready(roomCode)`
- `cancelReady(roomCode)`

### `websocket/MatchChannel.ts`
Methods:

- `startMatch(matchCode)`
- `sendPlayerAction(action)`
- `requestStateSync()`
- `endMatch(result)`

## 6. Shared Game SDK

### `sdk/GameRegistry.ts`
Methods:

- `register(gameMeta)`
- `getGame(gameCode)`
- `getAllGames()`

### `sdk/GameMeta.ts`
Fields:

- `gameCode`
- `gameName`
- `mode`
- `loader`
- `multiplayer`
- `routePath`

### `sdk/GameContext.ts`
Fields:

- `user`
- `gameCode`
- `roomInfo`
- `matchInfo`
- `config`
- `sdk`

### `sdk/GamePlatformSDK.ts`
Methods:

- `getCurrentUser()`
- `getGameConfig(gameCode)`
- `reportResult(result)`
- `showToast(message)`
- `playSound(name)`
- `updateAssetView()`
- `openResultModal(result)`

### `sdk/BaseGame.ts`
Methods:

- `init(container, context)`
- `start()`
- `pause()`
- `resume()`
- `restart()`
- `destroy()`
- `getSnapshot()`
- `handleServerMessage(message)`

### `sdk/BaseMultiplayerGame.ts`
Methods:

- `bindSocket(socketClient)`
- `sendAction(action)`
- `onPlayerJoined(player)`
- `onPlayerLeft(player)`
- `onStateSync(state)`
- `onMatchEnd(result)`

## 7. Shared Game Utilities

### `games/shared/CanvasGame.ts`
Methods:

- `mount(canvas)`
- `unmount()`
- `startLoop()`
- `stopLoop()`
- `update(deltaTime)`
- `render()`
- `resize()`

### `games/shared/InputManager.ts`
Methods:

- `bindKeyboard()`
- `bindMouse()`
- `unbindAll()`
- `isKeyPressed(key)`
- `getMousePosition()`

### `games/shared/AudioManager.ts`
Methods:

- `load(name, url)`
- `play(name)`
- `stop(name)`
- `toggleMute()`

### `games/shared/TimerManager.ts`
Methods:

- `start(name, duration)`
- `stop(name)`
- `getRemaining(name)`
- `clearAll()`

## 8. Game Folder Design

```text
games/
  gomoku/
    GomokuGame.ts
    GomokuRule.ts
    GomokuRenderer.ts
    GomokuInput.ts
    GomokuTypes.ts
  chinese-chess/
    ChineseChessGame.ts
    ChineseChessRule.ts
    ChineseChessRenderer.ts
    ChineseChessInput.ts
  blackjack/
    BlackjackGame.ts
    BlackjackRule.ts
    BlackjackRenderer.ts
  doodle/
    DoodleGame.ts
    DoodleRenderer.ts
    DoodleInput.ts
  draw-guess/
    DrawGuessGame.ts
    DrawGuessRenderer.ts
    DrawGuessInput.ts
    DrawGuessRound.ts
    DrawGuessTypes.ts
  shared/
```

## 9. Draw and Guess Frontend Design

### `DrawGuessGame.ts`
Methods:

- `init(container, context)`
- `startRound()`
- `setDrawer(playerId)`
- `submitGuess(text)`
- `sendStroke(strokeData)`
- `applyRemoteStroke(strokeData)`
- `endRound()`

### `DrawGuessRenderer.ts`
Methods:

- `renderCanvas()`
- `drawStroke(strokeData)`
- `clearCanvas()`
- `renderRoundInfo()`
- `renderGuessFeed()`

### `DrawGuessInput.ts`
Methods:

- `bindDrawingEvents()`
- `bindGuessInput()`
- `unbindAll()`
- `captureStroke()`

### `DrawGuessRound.ts`
Methods:

- `nextRound()`
- `isRoundOver()`
- `assignNextDrawer(players)`
- `calculateRoundScore(roundState)`

## 10. Admin Web Directory Structure

```text
admin-web/src/
  api/
  components/
  layouts/
  router/
  stores/
  views/
```

## 11. Admin Web API Files

### `api/adminUserApi.ts`
Methods:

- `pageUsers(params)`
- `getUserDetail(userId)`
- `banUser(userId)`
- `unbanUser(userId)`

### `api/adminAssetApi.ts`
Methods:

- `pageUserAssets(params)`
- `adjustCoin(data)`
- `adjustScore(data)`
- `getAssetLogs(userId)`

### `api/adminGameApi.ts`
Methods:

- `pageGames(params)`
- `saveGame(data)`
- `updateGame(data)`
- `updateGameConfig(data)`

### `api/adminRecordApi.ts`
Methods:

- `pageRecords(params)`
- `getMatchDetail(matchCode)`

### `api/adminDashboardApi.ts`
Methods:

- `getOverview()`
- `getGameStats()`
- `getOnlineStats()`
