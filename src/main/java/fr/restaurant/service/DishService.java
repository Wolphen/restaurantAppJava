package fr.restaurant.service;

import fr.restaurant.controller.SqliteController;
import fr.restaurant.model.Dish;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Service singleton : conserve la liste observable
 * et synchronise toutes les opérations avec la BDD.
 */
public final class DishService {

    /* ── Singleton ────────────────────────────────────────── */
    private static final DishService INSTANCE = new DishService();
    public static DishService getInstance() { return INSTANCE; }

    private final SqliteController db = new SqliteController();

    // données observables pour l’IHM
    private final ObservableList<Dish> dishes =
            FXCollections.observableArrayList(db.fetchDish());   // charge une fois

    private DishService() { }


    public ObservableList<Dish> getObservableList() { return dishes; }

    public void addDish(Dish d) {
        db.addDish(d);
        dishes.add(d);
    }

    public void deleteDish(Dish d) {
        db.deleteDish(d);
        dishes.remove(d);
    }

    // Re-synchronise entièrement depuis la BDD au besoin
    public void refresh() {
        dishes.setAll(db.fetchDish());
    }
}
