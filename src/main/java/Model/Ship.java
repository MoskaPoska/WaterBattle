package Model;

import java.util.List;

public class Ship {
    private int size;
    private List<Coordinate> coordinate;
    private boolean sunk;

    public Ship(int size, List<Coordinate> coordinate) {
        this.size = size;
        this.coordinate = coordinate;
        this.sunk = false;
    }
}
