# Backend Class Design

## 1. Package Structure

```text
com.webgame
  common
  security
  websocket
  modules
    auth
    user
    asset
    game
    room
    match
    record
    rank
    admin
```

## 2. Common Layer

### `ApiResponse<T>`
Methods:

- `success(T data)`
- `success(String message, T data)`
- `fail(String message)`
- `fail(String code, String message)`

### `PageResult<T>`
Fields:

- `records`
- `total`
- `pageNum`
- `pageSize`

### `BaseEntity`
Fields:

- `id`
- `createTime`
- `updateTime`
- `deleted`

### `BusinessException`
Methods:

- constructor with message
- constructor with code and message

### `GlobalExceptionHandler`
Methods:

- `handleBusinessException(...)`
- `handleMethodArgumentNotValidException(...)`
- `handleException(...)`

### `UserContext`
Methods:

- `getCurrentUserId()`
- `getCurrentUsername()`
- `hasRole(String roleCode)`
- `isAdmin()`

## 3. Security Layer

### `SecurityConfig`
Methods:

- `securityFilterChain(...)`
- `passwordEncoder()`
- `authenticationManager(...)`

### `JwtTokenProvider`
Methods:

- `generateAccessToken(Long userId, String username)`
- `generateRefreshToken(Long userId, String username)`
- `parseToken(String token)`
- `validateToken(String token)`

### `JwtAuthenticationFilter`
Methods:

- `doFilterInternal(...)`

### `CustomUserDetailsService`
Methods:

- `loadUserByUsername(String username)`

### `AuthUserDetails`
Fields:

- `userId`
- `username`
- `password`
- `authorities`

## 4. Auth Module

### `AuthController`
Methods:

- `register(RegisterRequest request)`
- `login(LoginRequest request)`
- `refreshToken(RefreshTokenRequest request)`
- `logout()`
- `profile()`

### `AuthService`
Methods:

- `register(RegisterRequest request)`
- `login(LoginRequest request)`
- `refreshToken(String refreshToken)`
- `getCurrentUserProfile()`

### DTOs

- `RegisterRequest`
- `LoginRequest`
- `RefreshTokenRequest`
- `LoginResponse`
- `UserProfileResponse`

## 5. User Module

### `UserEntity`
Fields:

- `id`
- `username`
- `nickname`
- `password`
- `avatar`
- `status`
- `lastLoginTime`

### `UserMapper`

### `UserService`
Methods:

- `getById(Long userId)`
- `findByUsername(String username)`
- `updateProfile(UpdateUserProfileRequest request)`
- `updateStatus(Long userId, Integer status)`

### `UserController`
Methods:

- `getCurrentUser()`
- `updateProfile(UpdateUserProfileRequest request)`
- `getUserDetail(Long userId)`

## 6. Asset Module

### `UserAssetEntity`
Fields:

- `userId`
- `score`
- `coin`
- `level`
- `exp`

### `AssetLogEntity`
Fields:

- `id`
- `userId`
- `assetType`
- `changeAmount`
- `beforeValue`
- `afterValue`
- `reason`
- `gameCode`
- `bizId`

### `AssetService`
Methods:

- `getUserAsset(Long userId)`
- `addCoin(Long userId, Integer amount, String reason)`
- `deductCoin(Long userId, Integer amount, String reason)`
- `addScore(Long userId, Integer amount, String reason)`
- `adjustAsset(AssetChangeRequest request)`
- `listAssetLogs(Long userId)`

### `AssetController`
Methods:

- `myAsset()`
- `myAssetLogs()`

### `AdminAssetController`
Methods:

- `pageUsersAsset(...)`
- `adjustUserCoin(AssetChangeRequest request)`
- `adjustUserScore(AssetChangeRequest request)`

## 7. Game Platform Module

### `GameEntity`
Fields:

- `id`
- `gameCode`
- `gameName`
- `gameType`
- `icon`
- `status`
- `supportsMultiplayer`
- `routePath`

### `GameConfigEntity`
Fields:

- `id`
- `gameCode`
- `configKey`
- `configValue`

### `GameService`
Methods:

- `listOnlineGames()`
- `getGameDetail(String gameCode)`
- `getGameConfigs(String gameCode)`
- `updateGameStatus(String gameCode, Integer status)`
- `updateGameConfig(GameConfigSaveRequest request)`

### `GameController`
Methods:

- `listGames()`
- `getGameDetail(String gameCode)`
- `getConfigs(String gameCode)`

### `AdminGameController`
Methods:

- `pageGames(...)`
- `saveGame(GameSaveRequest request)`
- `updateGame(GameUpdateRequest request)`
- `enableGame(String gameCode)`
- `disableGame(String gameCode)`
- `updateConfigs(GameConfigSaveRequest request)`

## 8. Room Module

### `GameRoomEntity`
Fields:

- `id`
- `roomCode`
- `gameCode`
- `roomStatus`
- `ownerUserId`
- `maxPlayers`
- `currentPlayers`

### `RoomPlayerEntity`
Fields:

- `id`
- `roomId`
- `userId`
- `seatNo`
- `readyStatus`
- `onlineStatus`

### `RoomService`
Methods:

- `createRoom(CreateRoomRequest request)`
- `joinRoom(String roomCode)`
- `leaveRoom(Long roomId, Long userId)`
- `dismissRoom(Long roomId)`
- `ready(Long roomId, Long userId)`
- `cancelReady(Long roomId, Long userId)`
- `startGameIfReady(Long roomId)`
- `getRoomDetail(String roomCode)`

### `RoomController`
Methods:

- `createRoom(CreateRoomRequest request)`
- `joinRoom(JoinRoomRequest request)`
- `leaveRoom(LeaveRoomRequest request)`
- `ready(RoomReadyRequest request)`
- `cancelReady(RoomReadyRequest request)`
- `detail(String roomCode)`

## 9. Match Module

### `GameMatchEntity`
Fields:

- `id`
- `matchCode`
- `gameCode`
- `roomId`
- `status`
- `startTime`
- `endTime`
- `winnerUserId`

### `MatchPlayerEntity`
Fields:

- `id`
- `matchId`
- `userId`
- `seatNo`
- `result`
- `score`

### `MatchService`
Methods:

- `createMatchFromRoom(Long roomId)`
- `startMatch(Long matchId)`
- `finishMatch(Long matchId, MatchResult result)`
- `abortMatch(Long matchId)`
- `submitPlayerAction(PlayerActionCommand command)`
- `getMatchSnapshot(Long matchId)`

### `GameRuleEngine`
Methods:

- `getGameCode()`
- `initState(MatchInitContext context)`
- `validateAction(PlayerActionCommand action, GameState state)`
- `applyAction(PlayerActionCommand action, GameState state)`
- `isGameOver(GameState state)`
- `buildMatchResult(GameState state)`
- `buildStateView(GameState state, Long viewerUserId)`

### Game rule engine implementations

- `GomokuRuleEngine`
- `ChineseChessRuleEngine`
- `BlackjackRuleEngine`
- `DoodleRuleEngine`
- `DrawGuessRuleEngine`

## 10. WebSocket Module

### `WebSocketConfig`
Methods:

- `registerWebSocketHandlers(...)`
- `configureMessageBroker(...)`

### `GameWebSocketHandler`
Methods:

- `afterConnectionEstablished(...)`
- `handleTextMessage(...)`
- `afterConnectionClosed(...)`
- `handleTransportError(...)`

### `SessionManager`
Methods:

- `bindUserSession(Long userId, WebSocketSession session)`
- `removeSession(Long userId)`
- `getSession(Long userId)`
- `sendToUser(Long userId, Object message)`
- `broadcastToRoom(Long roomId, Object message)`

### `GameMessageDispatcher`
Methods:

- `dispatch(Long userId, GameWsMessage message)`
- `handleRoomCommand(Long userId, GameWsMessage message)`
- `handleGameAction(Long userId, GameWsMessage message)`
- `handleHeartbeat(Long userId, GameWsMessage message)`

### `GameWsMessage`
Fields:

- `type`
- `gameCode`
- `roomCode`
- `matchCode`
- `payload`
- `timestamp`

### `WsMessageType`
Enum values:

- `HEARTBEAT`
- `ROOM_JOIN`
- `ROOM_LEAVE`
- `ROOM_READY`
- `ROOM_CANCEL_READY`
- `MATCH_START`
- `PLAYER_ACTION`
- `GAME_STATE_SYNC`
- `MATCH_END`
- `ERROR`

## 11. Record Module

### `GameRecordEntity`
Fields:

- `id`
- `gameCode`
- `matchCode`
- `userId`
- `result`
- `score`
- `coinReward`
- `durationSeconds`

### `RecordService`
Methods:

- `saveMatchRecords(MatchResult result)`
- `listUserRecords(Long userId, String gameCode)`
- `listGameRecords(String gameCode)`

### `RecordController`
Methods:

- `myRecords(...)`
- `gameRecords(...)`

## 12. Rank Module

### `RankService`
Methods:

- `getGlobalRank(String gameCode)`
- `getWeeklyRank(String gameCode)`
- `refreshRank(String gameCode)`
- `getUserRank(Long userId, String gameCode)`

### `RankController`
Methods:

- `globalRank(String gameCode)`
- `weeklyRank(String gameCode)`
- `myRank(String gameCode)`

## 13. Admin Module

### `AdminUserController`
Methods:

- `pageUsers(...)`
- `getUserDetail(Long userId)`
- `banUser(Long userId)`
- `unbanUser(Long userId)`

### `AdminDashboardController`
Methods:

- `overview()`
- `gameStats()`
- `onlineStats()`

### `AdminRecordController`
Methods:

- `pageRecords(...)`
- `matchDetail(String matchCode)`

### `AdminNoticeController`
Methods:

- `createNotice(NoticeSaveRequest request)`
- `updateNotice(NoticeSaveRequest request)`
- `deleteNotice(Long id)`
- `listNotices(...)`

## 14. Core Database Tables

- `user`
- `role`
- `user_role`
- `user_asset`
- `asset_log`
- `game`
- `game_config`
- `game_room`
- `room_player`
- `game_match`
- `match_player`
- `game_record`
- `game_rank`
- `notice`
- `admin_log`
