package org.example.waterbattle.Model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Ship implements Serializable {
    private final int countDeck;
    private List<Box>  boxesOfShip;
    private boolean isHorizontalPlacement;

    public Ship(int countDeck, boolean isHorizontalPlacement)
    {
        this.countDeck = countDeck;
        this.isHorizontalPlacement = isHorizontalPlacement;
        this.boxesOfShip = new ArrayList<>(countDeck);
    }
    public void createHorizontalShip(int x, int y)
    {
        int pointLimit = (Picture.COLUMNS - countDeck) * Picture.IMAGE_SIZE;
        for(int i = 0; i < countDeck; i++)
        {
            Box newBox;

            if(x > pointLimit)
            {
                newBox = new Box(Picture.SHIP, pointLimit + i * Picture.IMAGE_SIZE, y);
                boxesOfShip.add(newBox);
            }
            else {
                newBox = new Box(Picture.SHIP, (x + i * Picture.IMAGE_SIZE), y);
                boxesOfShip.add(newBox);
            }
        }
    }
    public void createVerticalShip(int x, int y)
    {
        int pointLimit = (Picture.ROWS - countDeck) * Picture.IMAGE_SIZE;
        for(int i = 0; i < countDeck; i++)
        {
            Box newBox;
            if(pointLimit < y)
            {
                newBox = new Box(Picture.SHIP, x, pointLimit + i * Picture.IMAGE_SIZE);
                boxesOfShip.add(newBox);
            }
            else {
                newBox = new Box(Picture.SHIP, x, (y + i * Picture.IMAGE_SIZE));
                boxesOfShip.add(newBox);
            }
        }
    }
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || o.getClass() != this.getClass()) return false;
        Ship ship = (Ship) o;
        return countDeck  == ship.getCountDeck()
                && (boxesOfShip != null && ship.getBoxesOfShip()!=null
                && boxesOfShip.hashCode() == ship.getBoxesOfShip().hashCode());
    }
}
