package com.webgame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;
    private final GameMessageDispatcher messageDispatcher;
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(SessionManager sessionManager, GameMessageDispatcher messageDispatcher, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.messageDispatcher = messageDispatcher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session.getUri());
        sessionManager.bindUserSession(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Long userId = extractUserId(session.getUri());
        GameWsMessage gameWsMessage;
        try {
            gameWsMessage = objectMapper.readValue(message.getPayload(), GameWsMessage.class);
        } catch (Exception exception) {
            gameWsMessage = new GameWsMessage(WsMessageType.HEARTBEAT, null, null, null, message.getPayload(), Instant.now());
        }
        messageDispatcher.dispatch(userId, gameWsMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(extractUserId(session.getUri()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessionManager.removeSession(extractUserId(session.getUri()));
    }

    private Long extractUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return 0L;
        }
        for (String pair : uri.getQuery().split("&")) {
            if (pair.startsWith("userId=")) {
                return Long.parseLong(pair.substring("userId=".length()));
            }
        }
        return 0L;
    }
}
