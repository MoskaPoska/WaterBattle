package org.example.waterbattle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.waterbattle.Model.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void sendMessage(WebSocketSession session, WebSocketMessage message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    public void sendMessageToPlayer(String playerId, WebSocketMessage message) {
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getKey().equals(playerId)) {
                sendMessage(entry.getValue(), message);
                break;
            }
        }
    }

    public void sendMessageToAll(String player1Id, String player2Id, WebSocketMessage message) {
        sendMessageToPlayer(player1Id, message);
        sendMessageToPlayer(player2Id, message);
    }
}