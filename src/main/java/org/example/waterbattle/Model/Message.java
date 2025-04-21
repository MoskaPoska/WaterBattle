package org.example.waterbattle.Model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Message implements Serializable {
    private int x;
    private int y;
    private MessageType type;
    private Box[][] gameField;
    private List<Ship> listOfShips;

    public Message(MessageType type, Box[][] gameField, List<Ship> listOfShips)
    {
        this.type = type;
        this.gameField = gameField;
        this.listOfShips = listOfShips;
    }
    public Message(MessageType type)
    {
        this.type = type;
    }
    public Message(MessageType type, int x, int y)
    {
        this.type = type;
        this.x = x;
        this.y = y;
    }
}
