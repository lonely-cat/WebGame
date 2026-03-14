package com.webgame.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SessionManager {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> roomMembers = new ConcurrentHashMap<>();

    public void bindUserSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    public void removeSession(Long userId) {
        userSessions.remove(userId);
        roomMembers.values().forEach(userIds -> userIds.remove(userId));
    }

    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    public void addUserToRoom(Long roomId, Long userId) {
        roomMembers.computeIfAbsent(roomId, key -> new HashSet<>()).add(userId);
    }

    public void removeUserFromRoom(Long roomId, Long userId) {
        roomMembers.computeIfPresent(roomId, (key, userIds) -> {
            userIds.remove(userId);
            return userIds;
        });
    }

    public Set<Long> getRoomMembers(Long roomId) {
        return new HashSet<>(roomMembers.getOrDefault(roomId, Set.of()));
    }

    public void sendToUser(Long userId, Object message) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(String.valueOf(message)));
        }
    }

    public void broadcastToRoom(Long roomId, Object message) {
        for (Long userId : new ArrayList<>(getRoomMembers(roomId))) {
            WebSocketSession session = userSessions.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(String.valueOf(message)));
                } catch (IOException ignored) {
                }
            }
        }
    }
}
