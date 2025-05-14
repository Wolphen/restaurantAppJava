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

    private final SqliteController db = new SqliteController();

    // Liste observable des tables (mise à jour automatiquement dans les vues)
    private final ObservableList<Table> tables =
            FXCollections.observableArrayList(db.fetchTables());

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

    // Méthode ajoutée pour récupérer les commandes associées à une table donnée
    // java
    public ObservableList<Order> getOrdersForTable(int tableId) {
        return FXCollections.observableArrayList(
                allOrders.stream()
                        .filter(order -> order.getTable() == tableId)
                        .collect(Collectors.toList())
        );
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