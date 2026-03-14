package com.webgame.websocket;

import org.springframework.stereotype.Component;

@Component
public class GameMessageDispatcher {

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
    }

    public void handleGameAction(Long userId, GameWsMessage message) {
    }

    public void handleHeartbeat(Long userId, GameWsMessage message) {
    }
}
