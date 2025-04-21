package org.example.waterbattle.service;

import org.example.waterbattle.Model.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Component
public class GameLogicService {
    public List<Box> generatedField()
    {
        List<Box> emptyField = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                emptyField.add(new Box(null, i*30, j*30));
            }
        }
        return emptyField;
    }
    public void processField(GameSession gameSession, String playerId, List<Box> field, List<Ship> ships) {
        if (gameSession.getPlayer1SessionId().equals(playerId)) {
            gameSession.setPlayer1Field(field);
            gameSession.setPlayer1Ships(ships);
        } else if (gameSession.getPlayer2SessionId().equals(playerId)) {
            gameSession.setPlayer2Field(field);
            gameSession.setPlayer2Ships(ships);
        }
    }
    public ShotResult processShot(GameSession gameSession, String playerId, int x, int y) {
        if (!gameSession.getCurrentTrun().equals(playerId)) {
            return new ShotResult(false, false, "It's not your turn.", x, y);
        }

        boolean hit = false;
        boolean sunk = false;
        String resultMessage = "Miss";
        List<Box> opponentField = null;
        List<Ship> opponentShips = null;

        String opponentId = gameSession.getPlayer1SessionId().equals(playerId) ?
                gameSession.getPlayer2SessionId() : gameSession.getPlayer1SessionId();

        if (gameSession.getPlayer1SessionId().equals(opponentId)) {
            opponentField = gameSession.getPlayer1Field();
            opponentShips = gameSession.getPlayer1Ships();
        } else if (gameSession.getPlayer2SessionId().equals(opponentId)) {
            opponentField = gameSession.getPlayer2Field();
            opponentShips = gameSession.getPlayer2Ships();
        }

        if (opponentField != null && opponentShips != null) {
            for (Box box : opponentField) {
                if (box.getX() / 30 == x && box.getY() / 30 == y && box.getPicture() == Picture.SHIP) {
                    hit = true;
                    box.setPicture(Picture.DESTROY_SHIP);


                    Ship sunkShip = checkIfSunk(opponentShips, x * 30, y * 30);
                    if (sunkShip != null) {
                        sunk = true;
                        resultMessage = "Sunk!";
                        markSunkShipOnField(opponentField, sunkShip);
                    } else {
                        resultMessage = "Hit!";
                    }
                    break;
                } else if (box.getX() / 30 == x && box.getY() / 30 == y && box.getPicture() == Picture.EMPTY) {
                    box.setPicture(Picture.POINT);
                }
            }
        }

        return new ShotResult(hit, sunk, resultMessage, x, y);
    }
    public Ship checkIfSunk(List<Ship> ships, int x, int y) {
        for (Ship ship : ships) {
            boolean hitPartFound = false;
            boolean allPartsHit = true;
            for (Box box : ship.getBoxesOfShip()) {
                if (box.getX() == x && box.getY() == y && box.getPicture() == Picture.DESTROY_SHIP) {
                    hitPartFound = true;
                }
                if (box.getPicture() == Picture.SHIP) {
                    allPartsHit = false;
                }
            }
            if (hitPartFound && allPartsHit) {
                return ship;
            }
        }
        return null;
    }

    public void markSunkShipOnField(List<Box> field, Ship sunkShip) {
        for (Box fieldBox : field) {
            for (Box shipBox : sunkShip.getBoxesOfShip()) {
                if (fieldBox.getX() == shipBox.getX() && fieldBox.getY() == shipBox.getY()) {
                    fieldBox.setPicture(Picture.DESTROY_SHIP);
                    break;
                }
            }
        }
    }
    public String determineFirstTurn(String player1Id, String player2Id) {
        Random random = new Random();

        if (random.nextInt(2) == 0) {
            return player1Id;
        } else {
            return player2Id;
        }
    }
}
