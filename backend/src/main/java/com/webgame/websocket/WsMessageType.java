package com.webgame.websocket;

public enum WsMessageType {
    HEARTBEAT,
    ROOM_JOIN,
    ROOM_LEAVE,
    ROOM_READY,
    ROOM_CANCEL_READY,
    MATCH_START,
    PLAYER_ACTION,
    GAME_STATE_SYNC,
    MATCH_END,
    ERROR
}
