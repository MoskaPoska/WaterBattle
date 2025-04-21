package org.example.waterbattle.session;

import org.example.waterbattle.Model.GameSession;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SessionAttributes
@Component
public class GameSessionManager {
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

    public GameSession findAvailableSession() {
        for (GameSession gameSession : gameSessions.values()) {
            if (gameSession.getPlayer2SessionId() == null) {
                return gameSession;
            }
        }
        return null;
    }

    public GameSession createNewSession(String player1Id) {
        GameSession newSession = new GameSession();
        newSession.setPlayer1SessionId(player1Id);
        gameSessions.put(newSession.getSessionId(), newSession);
        return newSession;
    }

    public GameSession getSessionByPlayerId(String playerId) {
        for (GameSession gameSession : gameSessions.values()) {
            if (gameSession.getPlayer1SessionId().equals(playerId) ||
                    gameSession.getPlayer2SessionId().equals(playerId)) {
                return gameSession;
            }
        }
        return null;
    }

    public void removePlayerFromSession(String playerId) {
        for (GameSession gameSession : gameSessions.values()) {
            if (gameSession.getPlayer1SessionId().equals(playerId)) {
                gameSession.setPlayer1SessionId(null);
            } else if (gameSession.getPlayer2SessionId().equals(playerId)) {
                gameSession.setPlayer2SessionId(null);
            }
        }
        removeEmptySessions();
    }

    public void removeSession(String sessionId) {
        gameSessions.remove(sessionId);
    }

    private void removeEmptySessions() {
        gameSessions.entrySet().removeIf(entry ->
                entry.getValue().getPlayer1SessionId() == null && entry.getValue().getPlayer2SessionId() == null
        );
    }
}
