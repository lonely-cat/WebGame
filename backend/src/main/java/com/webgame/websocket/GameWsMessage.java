package com.webgame.websocket;

import java.time.Instant;

public record GameWsMessage(
        WsMessageType type,
        String gameCode,
        String roomCode,
        String matchCode,
        String payload,
        Instant timestamp
) {
}
