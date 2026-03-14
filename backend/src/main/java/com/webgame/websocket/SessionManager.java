package com.webgame.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SessionManager {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void bindUserSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    public void removeSession(Long userId) {
        userSessions.remove(userId);
    }

    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    public void sendToUser(Long userId, Object message) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(String.valueOf(message)));
        }
    }

    public void broadcastToRoom(Long roomId, Object message) {
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("[room:%s] %s".formatted(roomId, message)));
                } catch (IOException ignored) {
                }
            }
        });
    }
}
