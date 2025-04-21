package org.example.waterbattle.Model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Box implements Serializable {
    private int x;
    private int y;
    private Picture picture;
    private Boolean isOpen = false;

    public Box(Picture picture, int x, int y) {
        this.picture = picture;
        this.x = x;
        this.y = y;
    }
}
