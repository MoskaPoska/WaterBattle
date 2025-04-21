package org.example.waterbattle.Model;

import lombok.Data;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

@Data
public class GameSession {
    private String sessionId = UUID.randomUUID().toString();
    private String player1SessionId;
    private String player2SessionId;
    private List<Box> player1Field;
    private List<Box> player2Field;
    private List<Ship> player1Ships;
    private List<Ship> player2Ships;
    private String currentTrun;
    private Boolean gameOver = false;
}
