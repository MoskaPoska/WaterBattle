package org.example.waterbattle.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final BattleShipWebSocketHandler battleShipWebSocketHandler;

    public WebSocketConfig(BattleShipWebSocketHandler battleShipWebSocketHandler) {
        this.battleShipWebSocketHandler = battleShipWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(battleShipWebSocketHandler, "/ws/battleship");
    }
}
