package com.webgame.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgame.modules.match.MatchServiceImpl;
import com.webgame.modules.room.RoomModels.GameRoomEntity;
import com.webgame.modules.room.RoomServiceImpl;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameMessageDispatcher {

    private final RoomServiceImpl roomService;
    private final MatchServiceImpl matchService;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public GameMessageDispatcher(RoomServiceImpl roomService, MatchServiceImpl matchService, SessionManager sessionManager,
                                 ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.matchService = matchService;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void dispatch(Long userId, GameWsMessage message) {
        if (message.type() == null) {
            return;
        }
        switch (message.type()) {
            case ROOM_JOIN, ROOM_LEAVE, ROOM_READY, ROOM_CANCEL_READY -> handleRoomCommand(userId, message);
            case PLAYER_ACTION, MATCH_START, MATCH_END, GAME_STATE_SYNC -> handleGameAction(userId, message);
            case HEARTBEAT -> handleHeartbeat(userId, message);
            case ERROR -> {
            }
        }
    }

    public void handleRoomCommand(Long userId, GameWsMessage message) {
        try {
            switch (message.type()) {
                case ROOM_JOIN -> {
                    roomService.joinRoomAsPlayer(message.roomCode(), userId);
                    GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                    sessionManager.addUserToRoom(room.getId(), userId);
                    broadcastRoomState(room);
                }
                case ROOM_LEAVE -> {
                    GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                    roomService.leaveRoom(room.getId(), userId);
                    sessionManager.removeUserFromRoom(room.getId(), userId);
                    broadcastRoomState(roomService.getRoomDetail(message.roomCode()));
                }
                case ROOM_READY -> {
                    GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                    roomService.ready(room.getId(), userId);
                    broadcastRoomState(room);
                }
                case ROOM_CANCEL_READY -> {
                    GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                    roomService.cancelReady(room.getId(), userId);
                    broadcastRoomState(room);
                }
                default -> {
                }
            }
        } catch (Exception exception) {
            sendError(userId, exception.getMessage());
        }
    }

    public void handleGameAction(Long userId, GameWsMessage message) {
        if (message.type() == WsMessageType.PLAYER_ACTION && message.roomCode() != null) {
            try {
                GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                Map<String, Object> payload = objectMapper.readValue(message.payload(), new TypeReference<>() {
                });
                MatchServiceImpl.MatchActionResult result = matchService.applyRoomAction(room, userId, payload);
                sessionManager.broadcastToRoom(room.getId(), serialize(new GameWsMessage(
                        WsMessageType.PLAYER_ACTION,
                        message.gameCode(),
                        message.roomCode(),
                        result.matchCode(),
                        serialize(result.action()),
                        java.time.Instant.now()
                )));
                sessionManager.broadcastToRoom(room.getId(), serialize(new GameWsMessage(
                        WsMessageType.GAME_STATE_SYNC,
                        message.gameCode(),
                        message.roomCode(),
                        result.matchCode(),
                        serialize(Map.of(
                                "state", result.state(),
                                "playerStones", result.playerStones()
                        )),
                        java.time.Instant.now()
                )));
                if (result.result() != null) {
                    sessionManager.broadcastToRoom(room.getId(), serialize(new GameWsMessage(
                            WsMessageType.MATCH_END,
                            message.gameCode(),
                            message.roomCode(),
                            result.matchCode(),
                            serialize(result.result()),
                            java.time.Instant.now()
                    )));
                }
            } catch (Exception exception) {
                sendError(userId, exception.getMessage());
            }
            return;
        }
        if (message.type() == WsMessageType.MATCH_START && message.roomCode() != null) {
            try {
                GameRoomEntity room = roomService.getRoomDetail(message.roomCode());
                roomService.startGameIfReady(room.getId());
                MatchServiceImpl.MatchStartResult result = matchService.startRoomMatch(room);
                sessionManager.broadcastToRoom(room.getId(), serialize(new GameWsMessage(
                        WsMessageType.MATCH_START,
                        room.gameCode,
                        room.roomCode,
                        result.matchCode(),
                        serialize(Map.of(
                                "status", "started",
                                "matchId", result.matchId(),
                                "matchCode", result.matchCode(),
                                "playerStones", result.playerStones()
                        )),
                        java.time.Instant.now()
                )));
                sessionManager.broadcastToRoom(room.getId(), serialize(new GameWsMessage(
                        WsMessageType.GAME_STATE_SYNC,
                        room.gameCode,
                        room.roomCode,
                        result.matchCode(),
                        serialize(Map.of(
                                "state", result.state(),
                                "playerStones", result.playerStones()
                        )),
                        java.time.Instant.now()
                )));
            } catch (Exception exception) {
                sendError(userId, exception.getMessage());
            }
        }
    }

    public void handleHeartbeat(Long userId, GameWsMessage message) {
        try {
            sessionManager.sendToUser(userId, serialize(new GameWsMessage(
                    WsMessageType.HEARTBEAT,
                    message.gameCode(),
                    message.roomCode(),
                    message.matchCode(),
                    "{\"status\":\"ok\"}",
                    java.time.Instant.now()
            )));
        } catch (Exception ignored) {
        }
    }

    private void broadcastRoomState(GameRoomEntity room) {
        sessionManager.broadcastToRoom(room.getId(), serialize(new RoomStateMessage(
                room.roomCode,
                room.gameCode,
                room.roomStatus,
                room.currentPlayers,
                room.maxPlayers,
                roomService.getRoomPlayers(room.getId())
        )));
    }

    private void sendError(Long userId, String message) {
        try {
            sessionManager.sendToUser(userId, serialize(new GameWsMessage(
                    WsMessageType.ERROR,
                    null,
                    null,
                    null,
                    "{\"message\":\"" + message + "\"}",
                    java.time.Instant.now()
            )));
        } catch (Exception ignored) {
        }
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{\"error\":\"serialization_failed\"}";
        }
    }

    private record RoomStateMessage(String roomCode, String gameCode, Integer roomStatus, Integer currentPlayers,
                                    Integer maxPlayers, Object players) {
    }
}
