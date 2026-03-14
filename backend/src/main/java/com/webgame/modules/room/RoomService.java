package com.webgame.modules.room;

import com.webgame.modules.room.RoomModels.CreateRoomRequest;
import com.webgame.modules.room.RoomModels.GameRoomEntity;

public interface RoomService {
    GameRoomEntity createRoom(CreateRoomRequest request);

    GameRoomEntity joinRoom(String roomCode);

    void leaveRoom(Long roomId, Long userId);

    void dismissRoom(Long roomId);

    void ready(Long roomId, Long userId);

    void cancelReady(Long roomId, Long userId);

    void startGameIfReady(Long roomId);

    GameRoomEntity getRoomDetail(String roomCode);
}
