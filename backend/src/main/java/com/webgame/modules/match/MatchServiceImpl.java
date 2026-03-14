package com.webgame.modules.match;

import com.webgame.common.BusinessException;
import com.webgame.modules.match.MatchModels.ActionValidateResult;
import com.webgame.modules.match.MatchModels.GameState;
import com.webgame.modules.match.MatchModels.GameStateView;
import com.webgame.modules.match.MatchModels.MatchInitContext;
import com.webgame.modules.match.MatchModels.MatchResult;
import com.webgame.modules.match.MatchModels.PlayerActionCommand;
import com.webgame.modules.room.RoomModels.GameRoomEntity;
import com.webgame.modules.room.RoomModels.RoomPlayerEntity;
import com.webgame.modules.room.RoomServiceImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl implements MatchService {

    private final AtomicLong matchIdSequence = new AtomicLong(1);
    private final Map<Long, Object> snapshots = new ConcurrentHashMap<>();
    private final Map<Long, ActiveMatchSession> sessionsByRoomId = new ConcurrentHashMap<>();
    private final Map<String, GameRuleEngine> ruleEnginesByGameCode = new ConcurrentHashMap<>();
    private final RoomServiceImpl roomService;

    public MatchServiceImpl(List<GameRuleEngine> ruleEngines, RoomServiceImpl roomService) {
        this.roomService = roomService;
        for (GameRuleEngine ruleEngine : ruleEngines) {
            ruleEnginesByGameCode.put(ruleEngine.getGameCode(), ruleEngine);
        }
    }

    @Override
    public Long createMatchFromRoom(Long roomId) {
        long matchId = matchIdSequence.getAndIncrement();
        snapshots.put(matchId, Map.of("roomId", roomId, "status", "created"));
        return matchId;
    }

    @Override
    public void startMatch(Long matchId) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "started"));
    }

    @Override
    public void finishMatch(Long matchId, MatchResult result) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "finished", "result", result));
    }

    @Override
    public void abortMatch(Long matchId) {
        snapshots.put(matchId, Map.of("matchId", matchId, "status", "aborted"));
    }

    @Override
    public void submitPlayerAction(PlayerActionCommand command) {
        if (command == null) {
            throw new BusinessException("MATCH_ACTION_INVALID", "player action is required");
        }
    }

    @Override
    public Object getMatchSnapshot(Long matchId) {
        return snapshots.getOrDefault(matchId, Map.of("matchId", matchId, "status", "missing"));
    }

    public MatchStartResult startRoomMatch(GameRoomEntity room) {
        GameRuleEngine ruleEngine = getRuleEngine(room.gameCode);
        List<RoomPlayerEntity> players = roomService.getRoomPlayers(room.getId());
        if (players.size() < 2 && "gomoku".equals(room.gameCode)) {
            throw new BusinessException("ROOM_NOT_ENOUGH_PLAYERS", "gomoku requires two players");
        }

        long matchId = createMatchFromRoom(room.getId());
        String matchCode = "match-" + matchId;
        GameState state = ruleEngine.initState(new MatchInitContext(room.getId(), room.gameCode));
        Map<Long, String> playerStones = assignRoles(room.gameCode, players);
        seedStateForRoles(room.gameCode, state, playerStones);
        ActiveMatchSession session = new ActiveMatchSession(matchId, matchCode, room.getId(), room.roomCode, room.gameCode, state, playerStones);
        sessionsByRoomId.put(room.getId(), session);
        startMatch(matchId);
        snapshots.put(matchId, buildSnapshot(session, ruleEngine.buildStateView(state, null)));
        return new MatchStartResult(matchId, matchCode, ruleEngine.buildStateView(state, null).visibleState(), session.playerStones());
    }

    public MatchActionResult applyRoomAction(GameRoomEntity room, Long userId, Map<String, Object> payload) {
        ActiveMatchSession session = sessionsByRoomId.get(room.getId());
        if (session == null) {
            throw new BusinessException("MATCH_NOT_STARTED", "match has not started yet");
        }
        GameRuleEngine ruleEngine = getRuleEngine(room.gameCode);
        String stone = session.playerStones().get(userId);
        if (stone == null) {
            throw new BusinessException("MATCH_PLAYER_INVALID", "player is not part of this match");
        }

        Map<String, Object> normalizedPayload = new HashMap<>(payload);
        normalizedPayload.put("userId", userId);
        normalizedPayload.put("stone", stone);
        normalizedPayload.put("role", stone);
        String actionType = String.valueOf(normalizedPayload.getOrDefault("type", "move"));
        PlayerActionCommand command = new PlayerActionCommand(room.gameCode, session.matchCode(), userId, actionType, normalizedPayload);
        ActionValidateResult validation = ruleEngine.validateAction(command, session.state());
        if (!validation.valid()) {
            throw new BusinessException("MATCH_ACTION_INVALID", validation.message());
        }

        GameState nextState = ruleEngine.applyAction(command, session.state());
        GameStateView view = ruleEngine.buildStateView(nextState, null);
        snapshots.put(session.matchId(), buildSnapshot(session, view));

        MatchResult result = null;
        if (ruleEngine.isGameOver(nextState)) {
            result = ruleEngine.buildMatchResult(nextState);
            finishMatch(session.matchId(), result);
        }

        return new MatchActionResult(
                session.matchCode(),
                normalizedPayload,
                view.visibleState(),
                session.playerStones(),
                result == null ? null : result.summary()
        );
    }

    public Map<String, Object> buildStateForUser(GameRoomEntity room, Long userId) {
        ActiveMatchSession session = sessionsByRoomId.get(room.getId());
        if (session == null) {
            return Map.of();
        }
        GameRuleEngine ruleEngine = getRuleEngine(room.gameCode);
        return ruleEngine.buildStateView(session.state(), userId).visibleState();
    }

    private Map<String, Object> buildSnapshot(ActiveMatchSession session, GameStateView view) {
        return Map.of(
                "matchId", session.matchId(),
                "matchCode", session.matchCode(),
                "roomCode", session.roomCode(),
                "gameCode", session.gameCode(),
                "state", view.visibleState()
        );
    }

    private Map<Long, String> assignRoles(String gameCode, List<RoomPlayerEntity> players) {
        Map<Long, String> playerStones = new HashMap<>();
        for (RoomPlayerEntity player : players) {
            if ("draw-guess".equals(gameCode)) {
                playerStones.put(player.userId, player.seatNo == 1 ? "drawer" : "guesser");
            } else if ("chinese-chess".equals(gameCode)) {
                playerStones.put(player.userId, player.seatNo == 1 ? "red" : "black");
            } else {
                playerStones.put(player.userId, player.seatNo == 1 ? "black" : "white");
            }
        }
        return playerStones;
    }

    private void seedStateForRoles(String gameCode, GameState state, Map<Long, String> playerRoles) {
        if ("draw-guess".equals(gameCode)) {
            Long drawerUserId = playerRoles.entrySet().stream()
                    .filter(entry -> "drawer".equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            state.data().put("drawerUserId", drawerUserId);
            state.data().put("currentTurn", "drawing");
            state.data().put("playerRoles", new HashMap<>(playerRoles));
        }
        if ("chinese-chess".equals(gameCode)) {
            state.data().put("currentTurn", "red");
            state.data().put("playerRoles", new HashMap<>(playerRoles));
        }
    }

    private GameRuleEngine getRuleEngine(String gameCode) {
        GameRuleEngine ruleEngine = ruleEnginesByGameCode.get(gameCode);
        if (ruleEngine == null) {
            throw new BusinessException("RULE_ENGINE_NOT_FOUND", "rule engine not found for game " + gameCode);
        }
        return ruleEngine;
    }

    private record ActiveMatchSession(Long matchId, String matchCode, Long roomId, String roomCode, String gameCode,
                                      GameState state, Map<Long, String> playerStones) {
    }

    public record MatchStartResult(Long matchId, String matchCode, Map<String, Object> state,
                                   Map<Long, String> playerStones) {
    }

    public record MatchActionResult(String matchCode, Map<String, Object> action, Map<String, Object> state,
                                    Map<Long, String> playerStones,
                                    Map<String, Object> result) {
    }
}
