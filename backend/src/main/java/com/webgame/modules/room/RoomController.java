package com.webgame.modules.room;

import com.webgame.common.ApiResponse;
import com.webgame.common.UserContext;
import com.webgame.modules.room.RoomModels.CreateRoomRequest;
import com.webgame.modules.room.RoomModels.JoinRoomRequest;
import com.webgame.modules.room.RoomModels.LeaveRoomRequest;
import com.webgame.modules.room.RoomModels.RoomReadyRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ApiResponse<?> createRoom(@RequestBody CreateRoomRequest request) {
        return ApiResponse.success(roomService.createRoom(request));
    }

    @PostMapping("/join")
    public ApiResponse<?> joinRoom(@RequestBody JoinRoomRequest request) {
        return ApiResponse.success(roomService.joinRoom(request.roomCode()));
    }

    @PostMapping("/leave")
    public ApiResponse<Void> leaveRoom(@RequestBody LeaveRoomRequest request) {
        roomService.leaveRoom(request.roomId(), UserContext.getCurrentUserId());
        return ApiResponse.success("left room", null);
    }

    @PostMapping("/ready")
    public ApiResponse<Void> ready(@RequestBody RoomReadyRequest request) {
        roomService.ready(request.roomId(), UserContext.getCurrentUserId());
        return ApiResponse.success("ready", null);
    }

    @PostMapping("/cancel-ready")
    public ApiResponse<Void> cancelReady(@RequestBody RoomReadyRequest request) {
        roomService.cancelReady(request.roomId(), UserContext.getCurrentUserId());
        return ApiResponse.success("cancelled", null);
    }

    @GetMapping("/detail")
    public ApiResponse<?> detail(@RequestParam String roomCode) {
        return ApiResponse.success(roomService.getRoomDetail(roomCode));
    }
}
