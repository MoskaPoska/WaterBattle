package org.example.waterbattle.Model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShotResult  implements Serializable {
    private boolean hit;
    private boolean sunk;
    private String resultMessage;
    private int x;
    private int y;
    private boolean yourTurn;
    private String nextPlayerId;

    public ShotResult(boolean hit, boolean sunk, String resultMessage, int x, int y) {
        this.hit = hit;
        this.sunk = sunk;
        this.resultMessage = resultMessage;
        this.x = x;
        this.y = y;
        this.yourTurn = false;
        this.nextPlayerId = null;
    }
}
