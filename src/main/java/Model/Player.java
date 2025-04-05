package Model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Player {
    private String name;
    private List<Ship> ships;
    private char[][] board;
    private int boardSize;

    public Player(String name)
    {
        this(name, 10);
    }

    public Player(String name, int boardSize)
    {
        this.name = name;
        this.ships = new ArrayList<>();
        this.boardSize = boardSize;
        this.board = new char[boardSize][boardSize];
        initializeBoard();
    }

    private void initializeBoard()
    {
        for(char [] row : board)
        {
            Arrays.fill(row, '~');
        }
    }
}
