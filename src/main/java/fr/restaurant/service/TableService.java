// java
package fr.restaurant.service;

import fr.restaurant.controller.SqliteController;
import fr.restaurant.model.Table;
import fr.restaurant.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class TableService {


    private static final TableService INSTANCE = new TableService();
    public static TableService getInstance() { return INSTANCE; }

    private final SqliteController db = new SqliteController();
    private final ObservableList<Table> tables =
            FXCollections.observableArrayList(db.fetchTables());

    // … addTable / assign / free inchangés …

    /** renvoie toujours les commandes à jour pour la table demandée */
    public ObservableList<Order> getOrdersForTable(int tableId) {
        return FXCollections.observableArrayList(
                db.fetchOrder().stream()
                        .filter(o -> o.getTable() == tableId)
                        .toList());
    }



    // Exemple de liste observable regroupant toutes les commandes courantes
    private final ObservableList<Order> allOrders =
            FXCollections.observableArrayList(db.fetchOrder());

    public ObservableList<Table> getTables() {
        return tables;
    }

    public void addTable(Table t) {
        db.addTable(t);
        tables.add(t);
    }


    public List<Table> listAvailableTables() {
        return tables.stream()
                .filter(t -> !t.isOccupied())
                .collect(Collectors.toList());
    }

    public boolean assignTable(int id) {
        return changeStatus(id, true);
    }

    public boolean freeTable(int id) {
        return changeStatus(id, false);
    }

    public List<Table> listAllTables() {
        return List.copyOf(tables);
    }

    private boolean changeStatus(int id, boolean occupied) {
        for (Table t : tables) {
            if (t.getId() == id) {
                if (t.isOccupied() == occupied) {
                    return false;
                }
                t.setOccupied(occupied);
                db.updateTableStatus(id, occupied);
                return true;
            }
        }
        return false;
    }
}