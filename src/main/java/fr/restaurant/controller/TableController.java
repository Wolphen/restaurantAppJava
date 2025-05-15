package fr.restaurant.controller;

import fr.restaurant.model.Table;
import fr.restaurant.model.Order;
import fr.restaurant.model.Dish;
import fr.restaurant.service.TableService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;
import java.util.stream.Collectors;

public class TableController {

    @FXML private TableView<Table>           tableView;
    @FXML private TableColumn<Table,Integer> idCol;
    @FXML private TableColumn<Table,Integer> sizeCol;
    @FXML private TableColumn<Table,Boolean> statusCol;

    @FXML private TextField idField;
    @FXML private TextField sizeField;

    // singleton pour bien partager l’état
    private final TableService service = TableService.getInstance();

    @FXML
    private void initialize() {
        // bind des colonnes → propriétés
        idCol  .setCellValueFactory(new PropertyValueFactory<>("id"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        statusCol.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isOccupied()).asObject()
        );

        // alimente le TableView
        tableView.setItems(service.getTables());

        // double-clic pour afficher la liste des commandes
        tableView.setRowFactory(tv -> {
            TableRow<Table> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    showOrders(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void onAddTable() {
        try {
            int id   = Integer.parseInt(idField.getText());
            int size = Integer.parseInt(sizeField.getText());

            if (service.getTables().stream().anyMatch(t -> t.getId() == id)) {
                alert("Id déjà pris, choisis un autre numéro");
                return;
            }
            service.addTable(new Table(id, size, false));
            idField.clear();
            sizeField.clear();
        } catch (NumberFormatException e) {
            alert("Attention, uniquement des chiffres !");
        }
    }

    @FXML private void onAssignTable() { toggleOccupied(true); }
    @FXML private void onFreeTable()   { toggleOccupied(false); }

    private void toggleOccupied(boolean occ) {
        Table t = tableView.getSelectionModel().getSelectedItem();
        if (t != null && (occ ? service.assignTable(t.getId())
                : service.freeTable(t.getId()))) {
            tableView.refresh();
        }
    }

    /**
     * affiche les commandes pour cette table dans une jolie fenêtre
     */
    private void showOrders(Table table) {
        // on relit la BDD à chaque fois → données fraîches
        ObservableList<Order> orders =
                FXCollections.observableArrayList(
                        service.getOrdersForTable(table.getId())
                );

        Stage stage = new Stage();
        stage.setTitle("Commandes — table " + table.getId());

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        if (orders.isEmpty()) {
            root.getChildren().add(new Label("Aucune commande pour cette table."));
        } else {
            for (Order o : orders) {

                // regroupe plats identiques → quantité
                Map<String, Long> counts = o.getDishes().stream()
                        .collect(Collectors.groupingBy(
                                Dish::getName, Collectors.counting()));

                ListView<String> list = new ListView<>();
                list.setMaxHeight(100);
                list.getItems().addAll(
                        counts.entrySet().stream()
                                .map(e -> e.getValue() + " × " + e.getKey())
                                .toList()
                );

                // header : id, prix (int), statut
                Label header = new Label(
                        "Cmd #" + o.getId() +
                                " · " + String.format("%d €", o.getGlobalPrice()) +
                                " · " + o.getStatus()
                );
                header.setStyle("-fx-font-weight:bold; -fx-underline:true;");

                VBox card = new VBox(6, header, list);
                card.setStyle("""
                    -fx-padding: 8;
                    -fx-background-color: #f5f5f5;
                    -fx-background-radius: 5;
                """);

                root.getChildren().add(card);
            }
        }

        stage.setScene(new Scene(root, 360, 300));
        stage.show();
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
