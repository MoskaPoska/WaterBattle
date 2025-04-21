package org.example.waterbattle.Model;

import lombok.Data;
import org.apache.logging.log4j.message.Message;

@Data
public class WebSocketMessage {
    private MessageType type;
    private Object payload;
    private Object additionalPayload;

    public WebSocketMessage(MessageType type, Object payload)
    {
        this.type = type;
        this.payload = payload;
    }
    public WebSocketMessage(MessageType type, Object payload, Object additionalPayload)
    {
        this.type = type;
        this.payload = payload;
        this.additionalPayload = additionalPayload;
    }
    public  WebSocketMessage(MessageType type, boolean hit, String message, int x, int y)
    {
        this.type = type;
        this.payload = hit;
        this.additionalPayload = message;
        this.additionalPayload = new int[] {x,y};
    }
}
