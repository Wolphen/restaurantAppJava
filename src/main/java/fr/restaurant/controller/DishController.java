package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DishController {

    /* ---------- injections FXML ---------- */
    @FXML private TableView<Dish> dishTable;

    @FXML private TableColumn<Dish,String>  nameCol;
    @FXML private TableColumn<Dish,Double>  priceCol;
    @FXML private TableColumn<Dish,String>  catCol;
    @FXML private TableColumn<Dish,String>  ingCol;     // liste d’ingrédients
    @FXML private TableColumn<Dish,Integer> countCol;   // nombre d’ingrédients

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField ingField;
    @FXML private TextField searchField;

    // donnée de test
    private final ObservableList<Dish> data = FXCollections.observableArrayList(
            new Dish("Pizza Margherita", 12.5, "Italien",
                    List.of("tomate", "mozzarella", "basilic")),
            new Dish("Burger Maison", 9.9, "Américain",
                    List.of("bœuf", "salade", "tomate", "cheddar")),
            new Dish("Curry Veggie", 11.0, "Indien",
                    List.of("pois chiches", "curry", "lait de coco"))
    );

    /* ---------- initialisation ---------- */
    @FXML
    private void initialize() {

        // liaison des colonnes
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        ingCol.setCellValueFactory(new PropertyValueFactory<>("ingredientsString"));
        countCol.setCellValueFactory(new PropertyValueFactory<>("ingredientCount"));

        // FilteredList pour la recherche
        FilteredList<Dish> filtered = new FilteredList<>(data, d -> true);

        // SortedList pour que le clic sur les titres déclenche le tri
        SortedList<Dish> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(dishTable.comparatorProperty());

        dishTable.setItems(sorted);

        // tri mono colonne tmtc
        dishTable.getSortOrder().addListener(
                (ListChangeListener<TableColumn<Dish, ?>>) change -> {
                    ObservableList<TableColumn<Dish, ?>> sort = dishTable.getSortOrder();
                    if (sort.size() > 1) {
                        TableColumn<Dish, ?> last = sort.get(sort.size() - 1);
                        sort.clear();
                        sort.add(last);
                    }
                });

        /* 5. recherche par ingrédient (inchangé) */
        searchField.textProperty().addListener((obs, o, n) -> {
            List<String> terms = Arrays.stream(n.toLowerCase().trim().split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .toList();

            filtered.setPredicate(dish -> terms.isEmpty() ||
                    terms.stream().allMatch(t ->
                            dish.getIngredients().stream()
                                    .anyMatch(ing -> ing.toLowerCase().contains(t))));
        });
    }

    // bouton Ajouter
    @FXML
    private void onAdd() {

        if (nameField.getText().isBlank() ||
                priceField.getText().isBlank() ||
                ingField.getText().isBlank()) return;

        try {
            double price = Double.parseDouble(priceField.getText());

            List<String> ings = Arrays.stream(ingField.getText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            data.add(new Dish(nameField.getText(), price, "Autre", ings));

            nameField.clear(); priceField.clear(); ingField.clear();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Prix invalide").showAndWait();
        }
    }
}
