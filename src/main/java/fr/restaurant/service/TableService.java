package fr.restaurant.service;

import fr.restaurant.model.Table;

import java.util.ArrayList;
import java.util.List;

public class TableService {

    private List<Table> tables;

    public TableService() {
        this.tables = new ArrayList<>();
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public List<Table> listAvailableTables() {
        List<Table> available = new ArrayList<>();
        for (Table table : tables) {
            if (!table.isOccupied()) {
                available.add(table);
            }
        }
        return available;
    }

    public boolean assignTable(int tableId) {
        for (Table table : tables) {
            if (table.getId() == tableId && !table.isOccupied()) {
                table.setOccupied(true);
                return true;
            }
        }
        return false;
    }

    public boolean freeTable(int tableId) {
        for (Table table : tables) {
            if (table.getId() == tableId && table.isOccupied()) {
                table.setOccupied(false);
                return true;
            }
        }
        return false;
    }

    public List<Table> listAllTables() {
        return new ArrayList<>(tables);
    }
}
