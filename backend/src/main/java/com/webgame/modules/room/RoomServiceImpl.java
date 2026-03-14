package com.webgame.modules.room;

import com.webgame.common.BusinessException;
import com.webgame.modules.room.RoomModels.CreateRoomRequest;
import com.webgame.modules.room.RoomModels.GameRoomEntity;
import com.webgame.modules.room.RoomModels.RoomPlayerEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {

    private final AtomicLong roomIdSequence = new AtomicLong(1);
    private final AtomicLong roomPlayerIdSequence = new AtomicLong(1);
    private final Map<Long, GameRoomEntity> roomsById = new ConcurrentHashMap<>();
    private final Map<String, Long> roomCodeIndex = new ConcurrentHashMap<>();
    private final Map<Long, List<RoomPlayerEntity>> playersByRoomId = new ConcurrentHashMap<>();

    @Override
    public synchronized GameRoomEntity createRoom(CreateRoomRequest request) {
        long roomId = roomIdSequence.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        GameRoomEntity room = new GameRoomEntity();
        room.setId(roomId);
        room.roomCode = generateRoomCode();
        room.gameCode = request.gameCode();
        room.roomStatus = 0;
        room.ownerUserId = null;
        room.maxPlayers = request.maxPlayers();
        room.currentPlayers = 0;
        room.setCreateTime(now);
        room.setUpdateTime(now);
        room.setDeleted(false);
        roomsById.put(roomId, room);
        roomCodeIndex.put(room.roomCode, roomId);
        playersByRoomId.put(roomId, new ArrayList<>());
        return room;
    }

    @Override
    public synchronized GameRoomEntity joinRoom(String roomCode) {
        GameRoomEntity room = getRoom(roomCode);
        if (room.currentPlayers >= room.maxPlayers) {
            throw new BusinessException("ROOM_FULL", "room is full");
        }
        room.currentPlayers = room.currentPlayers + 1;
        room.setUpdateTime(LocalDateTime.now());
        return room;
    }

    public synchronized RoomPlayerEntity joinRoomAsPlayer(String roomCode, Long userId) {
        GameRoomEntity room = joinRoom(roomCode);
        List<RoomPlayerEntity> players = playersByRoomId.computeIfAbsent(room.getId(), key -> new ArrayList<>());
        boolean exists = players.stream().anyMatch(player -> player.userId.equals(userId));
        if (exists) {
            return players.stream().filter(player -> player.userId.equals(userId)).findFirst().orElseThrow();
        }
        RoomPlayerEntity player = new RoomPlayerEntity();
        player.setId(roomPlayerIdSequence.getAndIncrement());
        player.roomId = room.getId();
        player.userId = userId;
        player.seatNo = players.size() + 1;
        player.readyStatus = 0;
        player.onlineStatus = 1;
        player.setCreateTime(LocalDateTime.now());
        player.setUpdateTime(LocalDateTime.now());
        player.setDeleted(false);
        players.add(player);
        return player;
    }

    @Override
    public synchronized void leaveRoom(Long roomId, Long userId) {
        List<RoomPlayerEntity> players = playersByRoomId.getOrDefault(roomId, new ArrayList<>());
        boolean removed = players.removeIf(player -> player.userId.equals(userId));
        if (removed) {
            GameRoomEntity room = roomsById.get(roomId);
            if (room != null && room.currentPlayers > 0) {
                room.currentPlayers = room.currentPlayers - 1;
                room.setUpdateTime(LocalDateTime.now());
            }
        }
    }

    @Override
    public void dismissRoom(Long roomId) {
        GameRoomEntity room = roomsById.remove(roomId);
        if (room != null) {
            roomCodeIndex.remove(room.roomCode);
        }
        playersByRoomId.remove(roomId);
    }

    @Override
    public synchronized void ready(Long roomId, Long userId) {
        RoomPlayerEntity player = getRoomPlayer(roomId, userId);
        player.readyStatus = 1;
        player.setUpdateTime(LocalDateTime.now());
    }

    @Override
    public synchronized void cancelReady(Long roomId, Long userId) {
        RoomPlayerEntity player = getRoomPlayer(roomId, userId);
        player.readyStatus = 0;
        player.setUpdateTime(LocalDateTime.now());
    }

    @Override
    public synchronized void startGameIfReady(Long roomId) {
        GameRoomEntity room = roomsById.get(roomId);
        if (room == null) {
            throw new BusinessException("ROOM_NOT_FOUND", "room not found");
        }
        List<RoomPlayerEntity> players = playersByRoomId.getOrDefault(roomId, List.of());
        if (players.isEmpty() || players.stream().anyMatch(player -> player.readyStatus == null || player.readyStatus != 1)) {
            throw new BusinessException("ROOM_NOT_READY", "not all players are ready");
        }
        room.roomStatus = 1;
        room.setUpdateTime(LocalDateTime.now());
    }

    @Override
    public GameRoomEntity getRoomDetail(String roomCode) {
        return getRoom(roomCode);
    }

    public List<RoomPlayerEntity> getRoomPlayers(Long roomId) {
        return new ArrayList<>(playersByRoomId.getOrDefault(roomId, List.of()));
    }

    private GameRoomEntity getRoom(String roomCode) {
        Long roomId = roomCodeIndex.get(roomCode);
        if (roomId == null || !roomsById.containsKey(roomId)) {
            throw new BusinessException("ROOM_NOT_FOUND", "room not found");
        }
        return roomsById.get(roomId);
    }

    private RoomPlayerEntity getRoomPlayer(Long roomId, Long userId) {
        return playersByRoomId.getOrDefault(roomId, List.of()).stream()
                .filter(player -> player.userId.equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("ROOM_PLAYER_NOT_FOUND", "player is not in room"));
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
