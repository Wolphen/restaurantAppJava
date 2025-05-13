package fr.restaurant.model;

public class Table {

    private int id;
    private int size;
    private boolean occupied;

    public Table(int id, int size, boolean occupied) {
        this.id = id;
        this.size = size;
        this.occupied = occupied;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", size=" + size +
                ", isOccupied=" + occupied +
                '}';
    }
}
