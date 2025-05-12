package fr.restaurant.service;

import fr.restaurant.model.Dish;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DishService {
    private static final DishService INSTANCE = new DishService();
    public static DishService getInstance() { return INSTANCE; }

    private final ObservableList<Dish> dishes = FXCollections.observableArrayList();

    public ObservableList<Dish> getObservableList() { return dishes; }

    public void addDish(Dish d) { dishes.add(d); }
}
