package Model;

public class Cell {
    private int row;
    private int col;
    private CellStatus status;

    public Cell(int row, int col, CellStatus status) {
        this.row = row;
        this.col = col;
        this.status = status;
    }

    public int getRaw() {return row;}
    public int getCol() {return col;}
    public CellStatus getStatus() {return status;}
    public void setStatus(CellStatus status) {this.status = status;}
    @Override
    public String toString() {
        return "[" + row + ", " + col + "] - " + status;
    }
}
