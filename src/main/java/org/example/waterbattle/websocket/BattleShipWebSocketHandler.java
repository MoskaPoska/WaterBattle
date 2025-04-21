package org.example.waterbattle.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.waterbattle.Model.*;
import org.example.waterbattle.service.GameLogicService;
import org.example.waterbattle.service.WebSocketService;
import org.example.waterbattle.session.GameSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Component
public class BattleShipWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(BattleShipWebSocketHandler.class);
    private final WebSocketService webSocketService;
    private final GameSessionManager gameSessionManager;
    private final GameLogicService gameLogicService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BattleShipWebSocketHandler(WebSocketService webSocketService, GameSessionManager gameSessionManager, GameLogicService gameLogicService) {
        this.webSocketService = webSocketService;
        this.gameSessionManager = gameSessionManager;
        this.gameLogicService = gameLogicService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        webSocketService.registerSession(session.getId(), session);
        assignPlayerToGame(session);
    }

    public void assignPlayerToGame(WebSocketSession session) throws IOException {
        GameSession availableSession = gameSessionManager.findAvailableSession();
        String playerId = session.getId();

        if (availableSession != null) {
            availableSession.setPlayer2SessionId(playerId);
            logger.info("Player {} joined session {}", playerId, availableSession.getSessionId());
            webSocketService.sendMessageToPlayer(availableSession.getPlayer1SessionId(),
                    new WebSocketMessage(MessageType.GAME_READY, "Opponent joined. Game ready"));
            webSocketService.sendMessage(session, new WebSocketMessage(MessageType.GAME_READY, "Joined the game, waiting for opponent's field"));
        } else {
            GameSession newSession = gameSessionManager.createNewSession(playerId);
            logger.info("Player {} created a new game session {}", playerId, newSession.getSessionId());
            webSocketService.sendMessage(session, new WebSocketMessage(MessageType.WAITING_FOR_OPPONENT, "Waiting for another player"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String playerId = session.getId();
        logger.info("Received message from {}: {}", playerId, message.getPayload());
        try {
            WebSocketMessage webSocketMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            GameSession gameSession = gameSessionManager.getSessionByPlayerId(playerId);



            if (gameSession == null) {
                logger.warn("Game session not found for player {}", playerId);
                webSocketService.sendMessage(session, new WebSocketMessage(MessageType.ERROR, "No active game session"));
                return;
            }

            switch (webSocketMessage.getType()) {
                case FIELD:
                    handleFieldMessage(playerId, gameSession, webSocketMessage);
                    break;
                case SHOT:
                    handleShotMessage(playerId, gameSession, webSocketMessage);
                    break;
                case DISCONNECT:
                    handleDisconnectMessage(playerId, gameSession);
                    break;
                case DEFEAT:
                    handleDefeatMessage(playerId, gameSession);
                    break;
                default:
                    logger.warn("Unknown message type: {}", webSocketMessage.getType());
                    webSocketService.sendMessage(session, new WebSocketMessage(MessageType.ERROR, "Unknown message type"));
            }
        } catch (IOException e) {
            logger.error("Error processing WebSocket message from {}: {}", playerId, e.getMessage());
            webSocketService.sendMessage(session, new WebSocketMessage(MessageType.ERROR, "Error processing message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String playerId = session.getId();
        logger.info("WebSocket connection closed for {}: {}", playerId, status);
        gameSessionManager.removePlayerFromSession(playerId);
        webSocketService.removeSession(playerId);
    }

    private void handleDefeatMessage(String playerId, GameSession gameSession) throws IOException {
        String opponentId = gameSession.getPlayer1SessionId().equals(playerId) ?
                gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();
        webSocketService.sendMessageToAll(playerId, opponentId, new WebSocketMessage(MessageType.DEFEAT_ACK, "Opponent has surrendered."));
        gameSessionManager.removePlayerFromSession(playerId);
    }

    private void handleDisconnectMessage(String playerId, GameSession gameSession) throws IOException {
        String opponentId = gameSession.getPlayer1SessionId().equals(playerId) ?
                gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();
        webSocketService.sendMessageToAll(playerId, opponentId, new WebSocketMessage(MessageType.OPPONENT_DISCONNECTED, "Opponent has disconnected"));
        gameSessionManager.removePlayerFromSession(playerId);
    }

    private void handleShotMessage(String playerId, GameSession gameSession, WebSocketMessage message) throws IOException {
        ShotResult shotResult = gameLogicService.processShot(gameSession, playerId,
                (int) message.getPayload(), (int) message.getAdditionalPayload());

        webSocketService.sendMessageToPlayer(playerId, new WebSocketMessage(MessageType.SHOT_RESULT, shotResult.isHit(), shotResult.getResultMessage(), shotResult.getX(), shotResult.getY()));
        String opponentId = gameSession.getPlayer1SessionId().equals(playerId) ?
                gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();
        webSocketService.sendMessageToPlayer(opponentId, new WebSocketMessage(MessageType.OPPONENT_SHOT, shotResult.getX(), shotResult.getY()));

        if (shotResult.isHit()) {

        }

        if (!shotResult.isYourTurn()) {
            webSocketService.sendMessageToPlayer(shotResult.getNextPlayerId(), new WebSocketMessage(MessageType.YOUR_TURN, "Your turn."));
            webSocketService.sendMessageToPlayer(playerId, new WebSocketMessage(MessageType.OPPONENT_TURN, "Opponent's turn."));
            gameSession.setCurrentTrun(shotResult.getNextPlayerId());
        }
    }

    private void handleFieldMessage(String playerId, GameSession gameSession, WebSocketMessage message) throws IOException {
        gameLogicService.processField(gameSession, playerId,
                (List<Box>) message.getPayload(),
                (List<org.example.waterbattle.Model.Ship>) message.getAdditionalPayload());

        if (gameSession.getPlayer1Field() != null && gameSession.getPlayer2Field() != null) {

            webSocketService.sendMessageToAll(gameSession.getPlayer1SessionId(), gameSession.getPlayer2SessionId(),
                    new WebSocketMessage(MessageType.OPPONENT_FIELD, gameLogicService.generatedField()));

            String startingPlayer = gameLogicService.determineFirstTurn(gameSession.getPlayer1SessionId(), gameSession.getPlayer2SessionId());
            gameSession.setCurrentTrun(startingPlayer);
            webSocketService.sendMessageToPlayer(startingPlayer, new WebSocketMessage(MessageType.YOUR_TURN, "Your turn."));
            String opponent = gameSession.getPlayer1SessionId().equals(startingPlayer) ?
                    gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();
            webSocketService.sendMessageToPlayer(opponent, new WebSocketMessage(MessageType.OPPONENT_TURN, "Opponent's turn."));

        } else {
            String waitingPlayerId = gameSession.getPlayer1SessionId().equals(playerId) ?
                    gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();
            if (waitingPlayerId != null) {
                webSocketService.sendMessageToPlayer(waitingPlayerId,
                        new WebSocketMessage(MessageType.WAITING_FOR_FIELD, "Waiting for opponent's field."));
            }
        }
    }
}