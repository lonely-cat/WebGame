package com.webgame.modules.room;

import com.webgame.common.BaseEntity;

public final class RoomModels {

    private RoomModels() {
    }

    public static class GameRoomEntity extends BaseEntity {
        public String roomCode;
        public String gameCode;
        public Integer roomStatus;
        public Long ownerUserId;
        public Integer maxPlayers;
        public Integer currentPlayers;
    }

    public static class RoomPlayerEntity extends BaseEntity {
        public Long roomId;
        public Long userId;
        public Integer seatNo;
        public Integer readyStatus;
        public Integer onlineStatus;
    }

    public record CreateRoomRequest(String gameCode, Integer maxPlayers) {
    }

    public record JoinRoomRequest(String roomCode) {
    }

    public record LeaveRoomRequest(Long roomId) {
    }

    public record RoomReadyRequest(Long roomId) {
    }
}
