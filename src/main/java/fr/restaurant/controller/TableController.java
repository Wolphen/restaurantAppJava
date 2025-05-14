// java
package fr.restaurant.controller;

import fr.restaurant.model.Table;
import fr.restaurant.model.Order;
import fr.restaurant.service.TableService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableController {

    @FXML
    private TableView<Table> tableView;
    @FXML
    private TableColumn<Table, Integer> idCol;
    @FXML
    private TableColumn<Table, Integer> sizeCol;
    @FXML
    private TableColumn<Table, Boolean> statusCol;

    @FXML
    private TextField idField;
    @FXML
    private TextField sizeField;

    private final TableService service = new TableService();

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        statusCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleBooleanProperty(cell.getValue().isOccupied()).asObject());

        tableView.setItems(service.getTables());

        // Ajout d'un écouteur sur double-clic pour afficher les commandes associées
        tableView.setRowFactory(tv -> {
            TableRow<Table> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Table clickedTable = row.getItem();
                    ObservableList<Order> orders = service.getOrdersForTable(clickedTable.getId());
                    StringBuilder info = new StringBuilder("Commandes pour la table " + clickedTable.getId() + " :\n");
                    for (Order order : orders) {
                        info.append(order.toString()).append("\n");
                    }
                    new Alert(Alert.AlertType.INFORMATION, info.toString()).showAndWait();
                }
            });
            return row;
        });
    }

    @FXML
    private void onAddTable() {
        try {
            int id = Integer.parseInt(idField.getText());
            int size = Integer.parseInt(sizeField.getText());
            if (tableView.getItems().stream().anyMatch(t -> t.getId() == id)) {
                new Alert(Alert.AlertType.ERROR, "id déjà pris, change de numéro").showAndWait();
                return;
            }
            service.addTable(new Table(id, size, false));
            idField.clear();
            sizeField.clear();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "chiffres uniquement").showAndWait();
        }
    }

    @FXML
    private void onAssignTable() {
        Table t = tableView.getSelectionModel().getSelectedItem();
        if (t != null && service.assignTable(t.getId())) {
            tableView.refresh();
        }
    }

    @FXML
    private void onFreeTable() {
        Table t = tableView.getSelectionModel().getSelectedItem();
        if (t != null && service.freeTable(t.getId())) {
            tableView.refresh();
        }
    }
}