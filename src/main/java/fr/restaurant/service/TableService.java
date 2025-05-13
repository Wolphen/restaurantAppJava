package fr.restaurant.service;

import fr.restaurant.controller.SqliteController;
import fr.restaurant.model.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * le boss des tables : stocke une liste observable et parle à sqlite.
 */
public class TableService {

    private final SqliteController db = new SqliteController();

    // liste qui se met à jour toute seule dans les vues
    private final ObservableList<Table> tables =
            FXCollections.observableArrayList(db.fetchTables());

    /* accès lecture – les contrôleurs l’utilisent directement */
    public ObservableList<Table> getTables() { return tables; }

    // ajoute une table (et la persiste)
    public void addTable(Table t) {
        db.addTable(t);
        tables.add(t);
    }

    // renvoie uniquement les tables libres
    public List<Table> listAvailableTables() {
        return tables.stream()
                .filter(t -> !t.isOccupied())
                .collect(Collectors.toList());
    }

    // tente d’occuper une table – true si succès
    public boolean assignTable(int id) {
        return changeStatus(id, true);
    }

    // libère une table – true si succès
    public boolean freeTable(int id) {
        return changeStatus(id, false);
    }

    // juste un raccourci pour avoir une copie
    public List<Table> listAllTables() {
        return List.copyOf(tables);
    }

    // implémentation commune
    private boolean changeStatus(int id, boolean occ) {
        for (Table t : tables) {
            if (t.getId() == id) {
                if (t.isOccupied() == occ) return false;   // déjà dans le bon état
                t.setOccupied(occ);
                db.updateTableStatus(id, occ);
                return true;
            }
        }
        return false; // id inconnu
    }
}
