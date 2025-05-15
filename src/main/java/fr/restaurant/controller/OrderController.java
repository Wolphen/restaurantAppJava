package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Order;
import fr.restaurant.model.Table;
import fr.restaurant.service.DishService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class OrderController {

    @FXML public TableView<Order> orderTable;
    @FXML public TableColumn<Order, Integer> tableCol;
    @FXML public TableColumn<Order, String> platCol;
    @FXML public TableColumn<Order, Float> priceCol;
    @FXML public TableColumn<Order, String> statusCol;
    @FXML public Button buttonAdd;
    public TableColumn<Order, Void> deleteCol;
    public TableColumn<Order, Void> doneCol;
    public static boolean canOrder = true;

    private final SqliteController db = new SqliteController();
    private final ObservableList<Order> orders = FXCollections.observableArrayList(db.fetchOrder());

    // Runnable pour mettre à jour le total
    private Runnable updateTotalPriceRunnable;

    @FXML
    private void initialize() {
        tableCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTable()).asObject());
        platCol.setCellValueFactory(cellData -> {
            String dishNames = cellData.getValue().getDishes().stream()
                    .map(Dish::getName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(dishNames);
        });
        priceCol.setCellValueFactory(new PropertyValueFactory<>("globalPrice"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        orderTable.setItems(orders);

        // Colonne pour annuler la commande
        deleteCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button btn = new Button("Annuler");
            {
                btn.setOnAction((ActionEvent e) -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("en cours".equals(order.getStatus())) {
                        db.cancelOrder(order.getId());
                        order.setStatus("cancel");
                        orderTable.refresh();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size() ||
                        !"en cours".equals(getTableView().getItems().get(getIndex()).getStatus())) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Colonne pour marquer la commande comme servie
        doneCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button btn = new Button("Servi");
            {
                btn.setOnAction((ActionEvent e) -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("en cours".equals(order.getStatus())) {
                        db.completeOrder(order.getId());
                        order.setStatus("completed");
                        orderTable.refresh();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size() ||
                        !"en cours".equals(getTableView().getItems().get(getIndex()).getStatus())) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    @FXML
    public void createOrder(){
        if (canOrder) {
            openOrderDialog();
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de créer une commande");
            alert.setContentText("Heure de commande dépassé (15min) ");
            alert.showAndWait();
        }

    }

    private void openOrderDialog() {
        Stage stage = new Stage();
        stage.setTitle("Créer une commande");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Sélection des tables disponibles non occupées
        Label tableLabel = new Label("Sélectionnez une table disponible :");
        ComboBox<Table> tableCombo = new ComboBox<>();
        List<Table> freeTables = db.fetchTables().stream()
                .filter(table -> !table.isOccupied())
                .collect(Collectors.toList());
        tableCombo.getItems().addAll(freeTables);
        tableCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Table item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "Table " + item.getId());
            }
        });
        tableCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Table item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "Table " + item.getId());
            }
        });

        // Zone de recherche pour filtrer les plats
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un plat");

        // TableView pour la sélection des plats
        Label dishLabel = new Label("Sélectionnez les plats :");
        TableView<DishSelection> dishTable = new TableView<>();
        ObservableList<DishSelection> dishSelections = FXCollections.observableArrayList();
        DishService.getInstance().getObservableList().forEach(d -> dishSelections.add(new DishSelection(d)));
        FilteredList<DishSelection> filteredDishes = new FilteredList<>(dishSelections, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.trim().toLowerCase();
            filteredDishes.setPredicate(ds -> ds.getDish().getName().toLowerCase().contains(query));
        });
        dishTable.setItems(filteredDishes);

        TableColumn<DishSelection, String> nameCol = new TableColumn<>("Plat");
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDish().getName()));

        TableColumn<DishSelection, Number> qtyCol = new TableColumn<>("Quantité");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());

        TableColumn<DishSelection, Void> minusCol = new TableColumn<>("Retirer");
        minusCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("-");
            {
                btn.setOnAction((ActionEvent event) -> {
                    DishSelection ds = getTableView().getItems().get(getIndex());
                    if (ds.getQuantity() > 0) {
                        ds.setQuantity(ds.getQuantity() - 1);
                        updateTotalPrice();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<DishSelection, Void> plusCol = new TableColumn<>("Ajouter");
        plusCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("+");
            {
                btn.setOnAction((ActionEvent event) -> {
                    DishSelection ds = getTableView().getItems().get(getIndex());
                    ds.setQuantity(ds.getQuantity() + 1);
                    updateTotalPrice();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        dishTable.getColumns().addAll(nameCol, minusCol, qtyCol, plusCol);
        dishTable.setPrefHeight(200);

        // Libellé affichant le prix total cumulé
        Label totalPriceLabel = new Label("Total : 0 €");

        updateTotalPriceRunnable = () -> {
            int total = dishSelections.stream()
                    .mapToInt(ds -> (int) (ds.getDish().getPrice() * ds.getQuantity()))
                    .sum();
            totalPriceLabel.setText("Total : " + total + " €");
        };

        Button confirmBtn = new Button("Confirmer la commande");
        confirmBtn.setOnAction(e -> {
            Table selectedTable = tableCombo.getSelectionModel().getSelectedItem();
            if (selectedTable == null) {
                new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner une table.").showAndWait();
                return;
            }
            List<Dish> selectedDishes = dishSelections.stream()
                    .filter(ds -> ds.getQuantity() > 0)
                    .flatMap(ds -> java.util.stream.Stream.generate(ds::getDish).limit(ds.getQuantity()))
                    .collect(Collectors.toList());
            if (selectedDishes.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner au moins un plat.").showAndWait();
                return;
            }
            int globalPrice = selectedDishes.stream()
                    .mapToInt(d -> (int) d.getPrice())
                    .sum();
            db.updateTableStatus(selectedTable.getId(), true);
            Order order = new Order(selectedTable.getId(), globalPrice, "en cours", selectedDishes);
            db.addOrder(order, selectedDishes);
            orders.add(order);
            stage.close();
        });

        root.getChildren().addAll(tableLabel, tableCombo, searchField, dishLabel, dishTable, totalPriceLabel, confirmBtn);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    private void updateTotalPrice() {
        if (updateTotalPriceRunnable != null) {
            updateTotalPriceRunnable.run();
        }
    }

    public class DishSelection {
        private final Dish dish;
        private final SimpleIntegerProperty quantity = new SimpleIntegerProperty(0);

        public DishSelection(Dish dish) {
            this.dish = dish;
        }

        public Dish getDish() {
            return dish;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int qty) {
            quantity.set(qty);
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }
    }

    public static void cantOrder(){
        canOrder = false;

    }
    public static void canOrder(){
        canOrder = true;
    }
}