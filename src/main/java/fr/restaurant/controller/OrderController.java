package fr.restaurant.controller;

import fr.restaurant.model.Dish;
import fr.restaurant.model.Order;
import fr.restaurant.model.Table;
import fr.restaurant.service.DishService;
import fr.restaurant.service.TableService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class OrderController {

    private final TableService tableService = TableService.getInstance();
    private final SqliteController db      = new SqliteController();

    // liste observable pour la TableView
    private final ObservableList<Order> orders =
            FXCollections.observableArrayList();

    @FXML public TableView<Order>    orderTable;
    @FXML public TableColumn<Order,Integer> tableCol;
    @FXML public TableColumn<Order,String>  platCol;
    @FXML public TableColumn<Order,Integer> priceCol;
    @FXML public TableColumn<Order,String>  statusCol;
    @FXML public TableColumn<Order,Void>    deleteCol;
    @FXML public TableColumn<Order,Void>    doneCol;
    @FXML public Button                   buttonAdd;

    // empêche les nouvelles commandes si time out
    public static boolean canOrder = true;
    private Runnable updateTotalPriceRunnable;

    @FXML
    private void initialize() {
        // colonnes de la TableView
        tableCol .setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getTable()).asObject());
        platCol  .setCellValueFactory(cd -> {
            String names = cd.getValue().getDishes().stream()
                    .map(Dish::getName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(names);
        });
        priceCol .setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getGlobalPrice()).asObject());
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // boutons d’action
        setupCancelColumn();
        setupDoneColumn();

        // branche la liste et charge initialement
        orderTable.setItems(orders);
        refreshOrders();
    }

    /** relit toutes les commandes en base et rafraîchit la TableView */
    private void refreshOrders() {
        orders.setAll(db.fetchOrder());
        orderTable.refresh();
    }

    /** colonne « Annuler » */
    private void setupCancelColumn() {
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Annuler");
            {
                btn.setOnAction((ActionEvent e) -> {
                    Order o = getTableView().getItems().get(getIndex());
                    if ("en cours".equalsIgnoreCase(o.getStatus())) {
                        db.cancelOrder(o.getId());
                        tableService.freeTable(o.getTable());
                        refreshOrders();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                        || getIndex() >= orders.size()
                        || !"en cours".equalsIgnoreCase(orders.get(getIndex()).getStatus())) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    /** colonne « Servi » */
    private void setupDoneColumn() {
        doneCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Servi");
            {
                btn.setOnAction((ActionEvent e) -> {
                    Order o = getTableView().getItems().get(getIndex());
                    if ("en cours".equalsIgnoreCase(o.getStatus())) {
                        db.completeOrder(o.getId());
                        tableService.freeTable(o.getTable());
                        refreshOrders();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                        || getIndex() >= orders.size()
                        || !"en cours".equalsIgnoreCase(orders.get(getIndex()).getStatus())) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    /** bouton « Nouvelle commande » */
    @FXML
    public void createOrder() {
        if (canOrder) {
            openOrderDialog();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Impossible de créer une commande\n" +
                            "Temps de commande dépassé (15 min)").showAndWait();
        }
    }

    /** fenêtre de création de commande */
    private void openOrderDialog() {
        Stage stage = new Stage();
        stage.setTitle("Créer une commande");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // 1) combo pour les tables libres
        ComboBox<Table> tableCombo = new ComboBox<>();
        List<Table> freeTables = db.fetchTables().stream()
                .filter(t -> !t.isOccupied())
                .collect(Collectors.toList());
        tableCombo.getItems().addAll(freeTables);

        // personnalisation de l'affichage "Table X"
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

        // 2) champ de recherche pour filtrer les plats
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un plat");

        // 3) tableau de sélection des plats (+ / - boutons)
        TableView<DishSelection> dishTable = buildDishSelectionTable(searchField);

        // 4) label du total
        Label totalPriceLabel = new Label("Total : 0 €");
        updateTotalPriceRunnable = () -> {
            double sum = dishTable.getItems().stream()
                    .mapToDouble(ds -> ds.getDish().getPrice() * ds.getQuantity())
                    .sum();
            totalPriceLabel.setText("Total : " + (int) sum + " €");
        };

        // 5) bouton Confirmer
        Button confirm = new Button("Confirmer la commande");
        confirm.setOnAction(e -> {
            Table selected = tableCombo.getValue();
            if (selected == null) {
                new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner une table.").showAndWait();
                return;
            }

            List<Dish> chosen = dishTable.getItems().stream()
                    .flatMap(ds -> java.util.stream.Stream.generate(ds::getDish)
                            .limit(ds.getQuantity()))
                    .collect(Collectors.toList());
            if (chosen.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner au moins un plat.").showAndWait();
                return;
            }

            // on calcule le total en sommant en double puis on cast en int
            double sum = chosen.stream()
                    .mapToDouble(Dish::getPrice)
                    .sum();
            int total = (int) sum;

            Order order = new Order(selected.getId(), total, "en cours", chosen);

            db.addOrder(order, chosen);                  // insertion en BDD
            tableService.assignTable(selected.getId());  // réserve la table
            refreshOrders();                             // rafraîchit la TableView

            stage.close();
        });

        // assemble la vue
        root.getChildren().addAll(
                new Label("Table :"), tableCombo,
                new Label("Plats :"), searchField, dishTable,
                totalPriceLabel, confirm
        );

        stage.setScene(new Scene(root, 400, 500));
        stage.showAndWait();
    }



    /** construit le TableView des plats avec +/- et filtre */
    private TableView<DishSelection> buildDishSelectionTable(TextField searchField) {
        ObservableList<DishSelection> list = FXCollections.observableArrayList();
        DishService.getInstance().getObservableList()
                .forEach(d -> list.add(new DishSelection(d)));
        FilteredList<DishSelection> filtered =
                new FilteredList<>(list, ds -> true);
        searchField.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase().trim();
            filtered.setPredicate(ds ->
                    ds.getDish().getName().toLowerCase().contains(q));
        });

        TableView<DishSelection> tv = new TableView<>(filtered);
        TableColumn<DishSelection,String> nameCol = new TableColumn<>("Plat");
        nameCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDish().getName()));

        TableColumn<DishSelection,Void> minusCol = new TableColumn<>("-");
        minusCol.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("-");
            { b.setOnAction(e -> {
                DishSelection ds = getTableView().getItems().get(getIndex());
                if (ds.getQuantity() > 0) {
                    ds.setQuantity(ds.getQuantity() - 1);
                    updateTotalPriceRunnable.run();
                }
            });}
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : b);
            }
        });

        TableColumn<DishSelection,Number> qtyCol = new TableColumn<>("Qté");
        qtyCol.setCellValueFactory(cd -> cd.getValue().quantityProperty());

        TableColumn<DishSelection,Void> plusCol = new TableColumn<>("+");
        plusCol.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("+");
            { b.setOnAction(e -> {
                DishSelection ds = getTableView().getItems().get(getIndex());
                ds.setQuantity(ds.getQuantity() + 1);
                updateTotalPriceRunnable.run();
            });}
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : b);
            }
        });

        tv.getColumns().addAll(nameCol, minusCol, qtyCol, plusCol);
        tv.setPrefHeight(200);
        return tv;
    }

    /** wrapper plat + quantité */
    public static class DishSelection {
        private final Dish dish;
        private final SimpleIntegerProperty quantity = new SimpleIntegerProperty(0);
        public DishSelection(Dish dish) { this.dish = dish; }
        public Dish getDish() { return dish; }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int q) { quantity.set(q); }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
    }

    public static void cantOrder() { canOrder = false; }
    public static void canOrder() { canOrder = true; }
}
